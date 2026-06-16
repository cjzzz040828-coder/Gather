package com.campus.biz.groupbuy.discount.impl;

import com.campus.biz.groupbuy.discount.DiscountCalculator;
import com.campus.biz.groupbuy.discount.DiscountContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 折扣：优惠后价 = 原价 * 折扣率（discountValue 为 0~1 的小数，如 0.8 表示 8 折；
 * 若 >1 视为「几折」按 10 折算，例如 8 -> 0.8）
 */
@Component
public class DiscountRateCalculator implements DiscountCalculator {

    @Override
    public Integer getDiscountType() {
        return 2;
    }

    @Override
    public BigDecimal calculate(DiscountContext context) {
        BigDecimal rate = context.discountValue();
        if (rate.compareTo(BigDecimal.ONE) > 0) {
            rate = rate.divide(BigDecimal.TEN, 4, RoundingMode.HALF_UP);
        }
        BigDecimal price = context.originalPrice().multiply(rate).setScale(2, RoundingMode.HALF_UP);
        return price.max(BigDecimal.ZERO);
    }
}
