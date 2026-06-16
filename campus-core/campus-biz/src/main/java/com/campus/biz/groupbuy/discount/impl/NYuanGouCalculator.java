package com.campus.biz.groupbuy.discount.impl;

import com.campus.biz.groupbuy.discount.DiscountCalculator;
import com.campus.biz.groupbuy.discount.DiscountContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * N元购：优惠后价 = 固定的 N 元（discountValue 即成交价，不超过原价）
 */
@Component
public class NYuanGouCalculator implements DiscountCalculator {

    @Override
    public Integer getDiscountType() {
        return 3;
    }

    @Override
    public BigDecimal calculate(DiscountContext context) {
        BigDecimal fixed = context.discountValue();
        if (fixed.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return fixed.min(context.originalPrice());
    }
}
