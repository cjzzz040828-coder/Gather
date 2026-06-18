package com.campus.biz.groupbuy.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 试算结果
 */
@Data
public class TrialResultDTO {

    /** 活动ID */
    private Long activityId;

    /** SKU ID */
    private Long skuId;

    /** 购买数量 */
    private Integer quantity;

    /** 原价（单件） */
    private BigDecimal originalPrice;

    /** 优惠金额（单件） */
    private BigDecimal deductionAmount;

    /** 优惠后实付价（单件） */
    private BigDecimal payPrice;

    /** 原价合计（单件原价 × 数量） */
    private BigDecimal totalOriginalPrice;

    /** 优惠合计（单件优惠 × 数量） */
    private BigDecimal totalDeductionAmount;

    /** 实付合计（单件实付价 × 数量） */
    private BigDecimal totalPayPrice;

    /** 成团目标人数 */
    private Integer targetCount;
}
