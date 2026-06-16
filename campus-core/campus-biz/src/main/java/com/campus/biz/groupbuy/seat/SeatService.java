package com.campus.biz.groupbuy.seat;

import com.campus.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 组队名额抢占服务：基于 Redis 原子递增 + SetNx 兜底占位实现前置名额抢占。
 *
 * <p>抢占在分布式锁外提供一道高性能、强一致的名额闸门：</p>
 * <ul>
 *   <li><b>原子递增</b>：{@code gb:seat:team:{teamId}} 记录团已抢占名额，incrementAndGet 后若超过目标人数则回退并拒绝，
 *       天然避免并发超卖（不依赖读-改-写的竞态窗口）。</li>
 *   <li><b>SetNx 兜底占位</b>：{@code gb:seat:user:{teamId}:{userId}} 以 setIfAbsent 占位，
 *       防止同一用户在同一团内并发重复抢占（DB 唯一校验之外的前置兜底）。</li>
 * </ul>
 *
 * <p>Redis 名额为前置闸门，MySQL stock 扣减仍作为最终一致兜底；下单失败时释放已抢占名额。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {

    private final RedissonClient redissonClient;

    private static final String TEAM_SEAT_PREFIX = "gb:seat:team:";
    private static final String USER_SEAT_PREFIX = "gb:seat:user:";
    /** 占位/计数键的兜底过期，避免异常残留（按团有效期上限给一天） */
    private static final Duration SEAT_TTL = Duration.ofDays(1);

    private String teamSeatKey(Long teamId) {
        return TEAM_SEAT_PREFIX + teamId;
    }

    private String userSeatKey(Long teamId, Long userId) {
        return USER_SEAT_PREFIX + teamId + ":" + userId;
    }

    /**
     * 抢占一个名额。成功返回，失败抛 BusinessException。
     *
     * @param teamId      团ID
     * @param userId      用户ID
     * @param targetCount 成团目标人数（名额上限）
     */
    public void acquire(Long teamId, Long userId, int targetCount) {
        // 1. SetNx 用户占位防重：同一用户在同一团内只能占一次
        RBucket<String> userBucket = redissonClient.getBucket(userSeatKey(teamId, userId));
        boolean firstHold = userBucket.setIfAbsent("1", SEAT_TTL);
        if (!firstHold) {
            throw new BusinessException("您已在该团中抢占名额，请勿重复参与");
        }

        // 2. 原子递增团名额：超过目标人数则回退并释放占位
        RAtomicLong teamSeat = redissonClient.getAtomicLong(teamSeatKey(teamId));
        long seat = teamSeat.incrementAndGet();
        if (seat == 1) {
            teamSeat.expire(SEAT_TTL);
        }
        if (seat > targetCount) {
            teamSeat.decrementAndGet();
            userBucket.delete();
            throw new BusinessException("该团名额已满");
        }
        log.debug("名额抢占成功: teamId={}, userId={}, seat={}/{}", teamId, userId, seat, targetCount);
    }

    /**
     * 释放名额（下单失败回滚 / 退单时调用）：团计数 -1 + 删除用户占位。
     */
    public void release(Long teamId, Long userId) {
        RAtomicLong teamSeat = redissonClient.getAtomicLong(teamSeatKey(teamId));
        if (teamSeat.isExists() && teamSeat.get() > 0) {
            teamSeat.decrementAndGet();
        }
        redissonClient.getBucket(userSeatKey(teamId, userId)).delete();
        log.debug("名额释放: teamId={}, userId={}", teamId, userId);
    }
}
