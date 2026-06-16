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

    /** 原价 */
    private BigDecimal originalPrice;

    /** 优惠金额 */
    private BigDecimal deductionAmount;

    /** 优惠后实付价 */
    private BigDecimal payPrice;

    /** 成团目标人数 */
    private Integer targetCount;
}
