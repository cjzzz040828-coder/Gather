package com.campus.biz.groupbuy.discount.impl;

import com.campus.biz.groupbuy.discount.DiscountCalculator;
import com.campus.biz.groupbuy.discount.DiscountContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 直减：优惠后价 = 原价 - 直减额（不低于 0）
 */
@Component
public class DirectReduceCalculator implements DiscountCalculator {

    @Override
    public Integer getDiscountType() {
        return 1;
    }

    @Override
    public BigDecimal calculate(DiscountContext context) {
        BigDecimal price = context.originalPrice().subtract(context.discountValue());
        return price.max(BigDecimal.ZERO);
    }
}
