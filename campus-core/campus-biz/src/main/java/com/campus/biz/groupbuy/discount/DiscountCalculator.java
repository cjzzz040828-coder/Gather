package com.campus.biz.groupbuy.discount;

import java.math.BigDecimal;

/**
 * 折扣计算策略（借鉴小傅哥试算规则树，按 discountType 枚举分发）
 */
public interface DiscountCalculator {

    /**
     * 该策略对应的折扣类型(1-直减 2-折扣 3-N元购)
     */
    Integer getDiscountType();

    /**
     * 计算优惠后价格（不低于 0）
     */
    BigDecimal calculate(DiscountContext context);
}
