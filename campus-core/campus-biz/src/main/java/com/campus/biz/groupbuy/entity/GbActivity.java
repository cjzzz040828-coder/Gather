package com.campus.biz.groupbuy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 拼团活动
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_gb_activity")
public class GbActivity extends BaseEntity {

    /** 商品ID */
    private Long goodsId;

    /** SKU ID */
    private Long skuId;

    /** 活动名称 */
    private String activityName;

    /** 成团人数 */
    private Integer targetCount;

    /** 活动开始时间 */
    private LocalDateTime validStart;

    /** 活动结束时间 */
    private LocalDateTime validEnd;

    /** 单团时限(分钟) */
    private Integer timeLimitMinutes;

    /** 可见人群标签(空=全部人群可见可参与；非空时仅命中该标签的用户可见可参与，由 Redis BitMap 判定) */
    private String visibleCrowdTag;

    /** 状态(0-未开始 1-进行中 2-已结束) */
    private Integer status;
}
