package com.campus.biz.groupbuy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 拼团折扣（与活动 1:N）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_gb_discount")
public class GbDiscount extends BaseEntity {

    /** 活动ID */
    private Long activityId;

    /** 折扣类型(1-直减 2-折扣 3-N元购) */
    private Integer discountType;

    /** 折扣值(直减额/折扣率/N元价) */
    private BigDecimal discountValue;

    /** 人群标签(空=全部人群) */
    private String crowdTag;

    /** 优先级(越大越优先) */
    private Integer priority;
}
