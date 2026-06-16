package com.campus.biz.groupbuy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 拼团组队（进度表）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_gb_team")
public class GbTeam extends BaseEntity {

    /** 活动ID */
    private Long activityId;

    /** 团长用户ID */
    private Long leaderId;

    /** 成团目标人数 */
    private Integer targetCount;

    /** 已锁单人数 */
    private Integer lockCount;

    /** 已支付人数 */
    private Integer completeCount;

    /** 状态(0-拼团中 1-成功 2-失败) */
    private Integer status;

    /** 本团截止时间(建团时刻+单团时限) */
    private LocalDateTime validEndTime;
}
