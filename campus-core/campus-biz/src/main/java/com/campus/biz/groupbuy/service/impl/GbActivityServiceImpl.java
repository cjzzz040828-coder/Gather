package com.campus.biz.groupbuy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.biz.groupbuy.entity.GbActivity;
import com.campus.biz.groupbuy.entity.GbDiscount;
import com.campus.biz.groupbuy.mapper.GbActivityMapper;
import com.campus.biz.groupbuy.mapper.GbDiscountMapper;
import com.campus.biz.groupbuy.service.GbActivityService;
import com.campus.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 拼团活动 Service 实现（含折扣维护）
 */
@Service
@RequiredArgsConstructor
public class GbActivityServiceImpl extends ServiceImpl<GbActivityMapper, GbActivity> implements GbActivityService {

    private final GbDiscountMapper discountMapper;

    /** 状态：未开始 / 进行中 / 已结束 */
    private static final int STATUS_NOT_STARTED = 0;
    private static final int STATUS_RUNNING = 1;
    private static final int STATUS_ENDED = 2;

    @Override
    public Page<GbActivity> adminPage(Integer page, Integer pageSize, Long goodsId, Integer status) {
        LambdaQueryWrapper<GbActivity> wrapper = new LambdaQueryWrapper<>();
        if (goodsId != null) {
            wrapper.eq(GbActivity::getGoodsId, goodsId);
        }
        if (status != null) {
            wrapper.eq(GbActivity::getStatus, status);
        }
        wrapper.orderByDesc(GbActivity::getId);
        return this.page(new Page<>(page, pageSize), wrapper);
    }

    @Override
    public List<GbActivity> listRunningByGoodsId(Long goodsId) {
        if (goodsId == null) {
            return new ArrayList<>();
        }
        return this.list(new LambdaQueryWrapper<GbActivity>()
                .eq(GbActivity::getGoodsId, goodsId)
                .eq(GbActivity::getStatus, STATUS_RUNNING)
                .orderByAsc(GbActivity::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveActivity(GbActivity activity, List<GbDiscount> discounts) {
        if (activity.getGoodsId() == null || activity.getSkuId() == null) {
            throw new BusinessException("必须选择商品与 SKU");
        }
        if (activity.getTargetCount() == null || activity.getTargetCount() < 2) {
            throw new BusinessException("成团人数至少为 2");
        }
        if (activity.getValidStart() == null || activity.getValidEnd() == null
                || !activity.getValidEnd().isAfter(activity.getValidStart())) {
            throw new BusinessException("活动时间区间不合法");
        }
        if (activity.getTimeLimitMinutes() == null || activity.getTimeLimitMinutes() <= 0) {
            throw new BusinessException("单团时限必须大于 0");
        }
        if (activity.getStatus() == null) {
            activity.setStatus(STATUS_NOT_STARTED);
        }
        this.saveOrUpdate(activity);
        Long activityId = activity.getId();

        // 折扣整单覆盖
        discountMapper.delete(new LambdaQueryWrapper<GbDiscount>().eq(GbDiscount::getActivityId, activityId));
        if (discounts != null) {
            for (GbDiscount discount : discounts) {
                if (discount.getDiscountType() == null || discount.getDiscountValue() == null) {
                    throw new BusinessException("折扣类型与折扣值不能为空");
                }
                discount.setId(null);
                discount.setActivityId(activityId);
                if (discount.getPriority() == null) {
                    discount.setPriority(0);
                }
                discountMapper.insert(discount);
            }
        }
        return activityId;
    }

    @Override
    public List<GbDiscount> listDiscounts(Long activityId) {
        if (activityId == null) {
            return new ArrayList<>();
        }
        return discountMapper.selectList(new LambdaQueryWrapper<GbDiscount>()
                .eq(GbDiscount::getActivityId, activityId)
                .orderByDesc(GbDiscount::getPriority));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        GbActivity activity = this.getById(id);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        if (status != STATUS_NOT_STARTED && status != STATUS_RUNNING && status != STATUS_ENDED) {
            throw new BusinessException("非法状态");
        }
        activity.setStatus(status);
        this.updateById(activity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeActivity(Long id) {
        GbActivity activity = this.getById(id);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        discountMapper.delete(new LambdaQueryWrapper<GbDiscount>().eq(GbDiscount::getActivityId, id));
        this.removeById(id);
    }
}
