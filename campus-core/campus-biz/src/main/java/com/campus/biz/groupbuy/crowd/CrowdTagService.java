package com.campus.biz.groupbuy.crowd;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 人群标签服务：基于 Redis BitMap 维护人群圈选。
 *
 * <p>每个标签对应一个 BitMap，key=gb:crowd:{tag}，以 userId 为 offset 置位。
 * 在试算/锁单链路中用于判定活动可见性与用户参与资格，支持运营侧按人群范围配置拼团活动。</p>
 *
 * <p>相比"从未拼单=newuser"这种实时 SQL 判定，BitMap 把人群圈选预计算后落到 Redis，
 * 判定 O(1)、内存紧凑（1 亿用户约 12MB），适合可见性这类高频读场景。</p>
 */
@Service
@RequiredArgsConstructor
public class CrowdTagService {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "gb:crowd:";

    private String key(String tag) {
        return KEY_PREFIX + tag;
    }

    /** 将用户加入人群标签 */
    public void addUser(String tag, long userId) {
        redisTemplate.opsForValue().setBit(key(tag), userId, true);
    }

    /** 批量将用户加入人群标签 */
    public void addUsers(String tag, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        String k = key(tag);
        for (Long userId : userIds) {
            if (userId != null) {
                redisTemplate.opsForValue().setBit(k, userId, true);
            }
        }
    }

    /** 将用户移出人群标签 */
    public void removeUser(String tag, long userId) {
        redisTemplate.opsForValue().setBit(key(tag), userId, false);
    }

    /** 判定用户是否命中人群标签 */
    public boolean contains(String tag, long userId) {
        Boolean bit = redisTemplate.opsForValue().getBit(key(tag), userId);
        return Boolean.TRUE.equals(bit);
    }
}
