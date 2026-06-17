package com.campus.biz.groupbuy.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.biz.groupbuy.dto.LockOrderDTO;
import com.campus.biz.groupbuy.dto.LockOrderResultDTO;
import com.campus.biz.groupbuy.dto.TrialResultDTO;
import com.campus.biz.groupbuy.address.UserAddress;
import com.campus.biz.groupbuy.address.UserAddressService;
import com.campus.biz.groupbuy.discount.DiscountContext;
import com.campus.biz.groupbuy.discount.DiscountCalculatorFactory;
import com.campus.biz.groupbuy.entity.*;
import com.campus.biz.groupbuy.event.TeamCompletedEvent;
import com.campus.biz.groupbuy.event.TeamFailedEvent;
import com.campus.biz.groupbuy.filter.SettlementChain;
import com.campus.biz.groupbuy.filter.SettlementContext;
import com.campus.biz.groupbuy.mapper.*;
import com.campus.biz.groupbuy.refund.RefundContext;
import com.campus.biz.groupbuy.refund.RefundStrategy;
import com.campus.biz.groupbuy.service.GbTradeService;
import com.campus.biz.groupbuy.seat.SeatService;
import com.campus.biz.groupbuy.trial.AccessGuard;
import com.campus.biz.groupbuy.trial.TrialChain;
import com.campus.biz.groupbuy.trial.TrialContext;
import com.campus.common.exception.BusinessException;
import com.campus.pay.PayServiceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 拼团核心交易 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GbTradeServiceImpl implements GbTradeService {

    private final GbActivityMapper activityMapper;
    private final GbDiscountMapper discountMapper;
    private final GbSkuMapper skuMapper;
    private final GbTeamMapper teamMapper;
    private final GbOrderMapper orderMapper;
    private final GbNotifyTaskMapper notifyTaskMapper;
    private final DiscountCalculatorFactory discountCalculatorFactory;
    private final SettlementChain settlementChain;
    private final PayServiceFactory payServiceFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;
    private final TrialChain trialChain;
    private final AccessGuard accessGuard;
    private final RedissonClient redissonClient;
    private final SeatService seatService;
    private final UserAddressService addressService;

    private static final int ACTIVITY_STATUS_RUNNING = 1;
    private static final int TEAM_STATUS_GROUPING = 0;
    private static final int TEAM_STATUS_FAILED = 2;
    private static final int ORDER_STATUS_LOCKED = 0;

    /** 锁单分布式锁：按团加锁，前缀 + teamId */
    private static final String TEAM_LOCK_PREFIX = "gb:lock:team:";
    private static final long LOCK_WAIT_SEC = 3;
    private static final long LOCK_LEASE_SEC = 10;

    @Override
    public TrialResultDTO trial(Long activityId, Long userId) {
        // 试算走规则树责任链：参数校验→动态开关→营销配置加载→人群标签过滤→优惠计算→异常兜底
        TrialContext context = trialChain.execute(activityId, userId);
        return context.getResult();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LockOrderResultDTO lockOrder(LockOrderDTO dto, Long userId) {
        GbActivity activity = getRunningActivity(dto.getActivityId());
        accessGuard.checkAccess(activity, userId);
        GbSku sku = skuMapper.selectById(activity.getSkuId());
        if (sku == null) {
            throw new BusinessException("SKU 不存在");
        }
        if (sku.getStock() == null || sku.getStock() <= 0) {
            throw new BusinessException("库存不足");
        }

        // 校验收货地址（实物商品必填，校验归属当前用户）
        if (dto.getAddressId() == null) {
            throw new BusinessException("请先选择收货地址");
        }
        UserAddress address = addressService.getMine(dto.getAddressId(), userId);
        if (address == null) {
            throw new BusinessException("收货地址不存在");
        }

        // 试算实付价
        BigDecimal originalPrice = sku.getOriginalPrice();
        BigDecimal payPrice = applyBestDiscount(dto.getActivityId(), originalPrice, accessGuard.userCrowdTag(userId));

        // 建团 or 加团
        GbTeam team = resolveTeam(dto, activity, userId);

        // 用 Redisson 分布式锁按团串行化"抢名额→写单→扣库存"临界区，防并发超员/超卖
        RLock lock = redissonClient.getLock(TEAM_LOCK_PREFIX + team.getId());
        boolean locked;
        try {
            locked = lock.tryLock(LOCK_WAIT_SEC, LOCK_LEASE_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统繁忙，请稍后重试");
        }
        if (!locked) {
            throw new BusinessException("当前参与人数较多，请稍后重试");
        }
        try {
            // Redis 原子递增 + SetNx 兜底占位抢名额（前置闸门）
            seatService.acquire(team.getId(), userId, activity.getTargetCount());
            try {
                return doLockOrder(dto, activity, sku, team, userId, originalPrice, payPrice, address);
            } catch (RuntimeException e) {
                // 下单失败，释放已抢占的 Redis 名额（DB 由事务回滚）
                seatService.release(team.getId(), userId);
                throw e;
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 锁内下单：防重校验 → 写锁定单 → 扣库存 → 团锁单数+1 → 调支付下单。
     */
    private LockOrderResultDTO doLockOrder(LockOrderDTO dto, GbActivity activity, GbSku sku,
                                           GbTeam team, Long userId,
                                           BigDecimal originalPrice, BigDecimal payPrice,
                                           UserAddress address) {
        // 防止同一用户在同一团内重复锁单
        Long dup = orderMapper.selectCount(new LambdaQueryWrapper<GbOrder>()
                .eq(GbOrder::getTeamId, team.getId())
                .eq(GbOrder::getUserId, userId));
        if (dup != null && dup > 0) {
            throw new BusinessException("您已在该团中拼单，请勿重复参与");
        }

        // 写拼单记录（锁定）
        String outTradeNo = "GB" + IdUtil.getSnowflakeNextIdStr();
        GbOrder order = new GbOrder();
        order.setTeamId(team.getId());
        order.setActivityId(activity.getId());
        order.setSkuId(sku.getId());
        order.setUserId(userId);
        order.setOutTradeNo(outTradeNo);
        order.setOriginalAmount(originalPrice);
        order.setPayAmount(payPrice);
        order.setDeductionAmount(originalPrice.subtract(payPrice));
        order.setStatus(ORDER_STATUS_LOCKED);
        // 收货地址快照（固化下单时的地址，后续地址修改不影响本单）
        order.setReceiver(address.getReceiver());
        order.setPhone(address.getPhone());
        order.setAddress(buildAddressText(address));
        try {
            orderMapper.insert(order);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("交易单号重复，请重试");
        }

        // 锁库存 + 团锁单数 +1（MySQL 最终一致兜底）
        int updated = skuMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<GbSku>()
                .eq(GbSku::getId, sku.getId())
                .gt(GbSku::getStock, 0)
                .setSql("stock = stock - 1"));
        if (updated == 0) {
            throw new BusinessException("库存不足");
        }
        team.setLockCount((team.getLockCount() == null ? 0 : team.getLockCount()) + 1);
        teamMapper.updateById(team);

        // 调支付下单（模拟通道）
        Map<String, String> payParams = payServiceFactory.createPayOrder(
                outTradeNo, payPrice, activity.getActivityName());

        LockOrderResultDTO result = new LockOrderResultDTO();
        result.setOrderId(order.getId());
        result.setTeamId(team.getId());
        result.setOutTradeNo(outTradeNo);
        result.setPayParams(payParams);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payCallback(String outTradeNo) {
        SettlementContext context = settlementChain.execute(outTradeNo);
        if (context.isTeamCompleted()) {
            GbTeam team = context.getTeam();
            // 写本地消息表（与结算同事务），事务提交后由监听器异步消费
            GbNotifyTask task = new GbNotifyTask();
            task.setBizId(team.getId());
            task.setMessageType("team_complete");
            task.setMessageBody(JSONUtil.toJsonStr(Map.of(
                    "teamId", team.getId(),
                    "activityId", team.getActivityId(),
                    "completeAt", LocalDateTime.now().toString())));
            task.setStatus(0);
            task.setRetryCount(0);
            notifyTaskMapper.insert(task);

            eventPublisher.publishEvent(new TeamCompletedEvent(team.getId(), team.getActivityId()));
            log.info("拼团成团: teamId={}, activityId={}", team.getId(), team.getActivityId());
        }
    }

    @Override
    public int closeTimeoutTeams() {
        List<GbTeam> timeoutTeams = teamMapper.selectList(new LambdaQueryWrapper<GbTeam>()
                .eq(GbTeam::getStatus, TEAM_STATUS_GROUPING)
                .lt(GbTeam::getValidEndTime, LocalDateTime.now()));
        int closed = 0;
        for (GbTeam team : timeoutTeams) {
            try {
                // 逐团独立事务：单团失败不影响其它团
                transactionTemplate.executeWithoutResult(s -> closeOneTeam(team.getId()));
                closed++;
            } catch (Exception e) {
                log.error("超时收尾失败: teamId={}", team.getId(), e);
            }
        }
        return closed;
    }

    /**
     * 收尾单个超时团（事务内）：再校验状态 → 退单团内每笔订单 → 团置失败 → 写 team_fail 消息 + 发事件。
     */
    private void closeOneTeam(Long teamId) {
        GbTeam team = teamMapper.selectById(teamId);
        // 二次校验：可能已被支付回调成团或被其它扫描收尾
        if (team == null || team.getStatus() != TEAM_STATUS_GROUPING) {
            return;
        }
        if (team.getValidEndTime() == null || LocalDateTime.now().isBefore(team.getValidEndTime())) {
            return;
        }

        List<GbOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<GbOrder>()
                .eq(GbOrder::getTeamId, teamId));
        for (GbOrder order : orders) {
            RefundContext ctx = new RefundContext(order, orderMapper, skuMapper, teamMapper, payServiceFactory);
            RefundStrategy.from(order).refund(ctx);
            // 释放该用户在团内抢占的 Redis 名额，保持与 DB 一致
            seatService.release(teamId, order.getUserId());
        }

        // 仅改 status，避免覆盖退单已扣减的 lock_count（refund 用 setSql 直接改了 DB）
        teamMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<GbTeam>()
                .eq(GbTeam::getId, team.getId())
                .set(GbTeam::getStatus, TEAM_STATUS_FAILED));

        GbNotifyTask task = new GbNotifyTask();
        task.setBizId(team.getId());
        task.setMessageType("team_fail");
        task.setMessageBody(JSONUtil.toJsonStr(Map.of(
                "teamId", team.getId(),
                "activityId", team.getActivityId(),
                "failAt", LocalDateTime.now().toString())));
        task.setStatus(0);
        task.setRetryCount(0);
        notifyTaskMapper.insert(task);

        eventPublisher.publishEvent(new TeamFailedEvent(team.getId(), team.getActivityId()));
        log.info("拼团超时失败收尾: teamId={}, activityId={}, 退单数={}", team.getId(), team.getActivityId(), orders.size());
    }

    // ===== 私有辅助 =====

    private GbActivity getRunningActivity(Long activityId) {
        GbActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        if (activity.getStatus() != ACTIVITY_STATUS_RUNNING) {
            throw new BusinessException("活动未在进行中");
        }
        LocalDateTime now = LocalDateTime.now();
        if (activity.getValidStart() != null && now.isBefore(activity.getValidStart())) {
            throw new BusinessException("活动尚未开始");
        }
        if (activity.getValidEnd() != null && now.isAfter(activity.getValidEnd())) {
            throw new BusinessException("活动已结束");
        }
        return activity;
    }

    /**
     * 选命中的最高优先级折扣并计算实付价；无命中折扣则原价。
     */
    private BigDecimal applyBestDiscount(Long activityId, BigDecimal originalPrice, String userCrowdTag) {
        List<GbDiscount> discounts = discountMapper.selectList(new LambdaQueryWrapper<GbDiscount>()
                .eq(GbDiscount::getActivityId, activityId)
                .orderByDesc(GbDiscount::getPriority));
        for (GbDiscount discount : discounts) {
            String tag = discount.getCrowdTag();
            boolean hit = (tag == null || tag.isBlank()) || tag.equals(userCrowdTag);
            if (hit) {
                DiscountContext ctx = new DiscountContext(
                        originalPrice, discount.getDiscountValue(), tag, userCrowdTag);
                return discountCalculatorFactory.get(discount.getDiscountType()).calculate(ctx);
            }
        }
        return originalPrice;
    }

    private GbTeam resolveTeam(LockOrderDTO dto, GbActivity activity, Long userId) {
        if (dto.getTeamId() != null) {
            GbTeam team = teamMapper.selectById(dto.getTeamId());
            if (team == null) {
                throw new BusinessException("组队不存在");
            }
            if (team.getStatus() != TEAM_STATUS_GROUPING) {
                throw new BusinessException("该团已结束，无法加入");
            }
            if (team.getValidEndTime() != null && LocalDateTime.now().isAfter(team.getValidEndTime())) {
                throw new BusinessException("该团已超时，无法加入");
            }
            if (team.getLockCount() != null && team.getLockCount() >= team.getTargetCount()) {
                throw new BusinessException("该团已满");
            }
            return team;
        }
        // 新建团，发起人为团长
        GbTeam team = new GbTeam();
        team.setActivityId(activity.getId());
        team.setLeaderId(userId);
        team.setTargetCount(activity.getTargetCount());
        team.setLockCount(0);
        team.setCompleteCount(0);
        team.setStatus(TEAM_STATUS_GROUPING);
        team.setValidEndTime(LocalDateTime.now().plusMinutes(activity.getTimeLimitMinutes()));
        teamMapper.insert(team);
        return team;
    }

    /** 拼接收货地址快照文本：省市区 + 详细 + 收货人电话 */
    private String buildAddressText(UserAddress a) {
        StringBuilder sb = new StringBuilder();
        if (a.getProvince() != null) sb.append(a.getProvince());
        if (a.getCity() != null) sb.append(a.getCity());
        if (a.getDistrict() != null) sb.append(a.getDistrict());
        if (a.getDetail() != null) sb.append(a.getDetail());
        return sb.toString();
    }
}
