package com.campus.biz.groupbuy.discount;

import java.math.BigDecimal;

/**
 * 试算上下文：原价 + 折扣值 + 人群标签
 */
public record DiscountContext(BigDecimal originalPrice, BigDecimal discountValue, String crowdTag, String userCrowdTag) {
}
