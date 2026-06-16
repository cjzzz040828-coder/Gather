package com.campus.biz.groupbuy.trial.node;

import com.campus.biz.groupbuy.discount.DiscountCalculatorFactory;
import com.campus.biz.groupbuy.discount.DiscountContext;
import com.campus.biz.groupbuy.entity.GbDiscount;
import com.campus.biz.groupbuy.trial.TrialContext;
import com.campus.biz.groupbuy.trial.TrialNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 节点5·优惠计算：按优先级选中命中的最高优先级折扣并计算实付价；无命中折扣则原价。
 */
@Component
@RequiredArgsConstructor
public class DiscountCalcNode implements TrialNode {

    private final DiscountCalculatorFactory discountCalculatorFactory;

    @Override
    public int order() {
        return 50;
    }

    @Override
    public void handle(TrialContext context) {
        BigDecimal originalPrice = context.getOriginalPrice();
        String userCrowdTag = context.getUserCrowdTag();
        List<GbDiscount> discounts = context.getDiscounts();

        BigDecimal payPrice = originalPrice;
        if (discounts != null) {
            for (GbDiscount discount : discounts) {
                String tag = discount.getCrowdTag();
                boolean hit = (tag == null || tag.isBlank()) || tag.equals(userCrowdTag);
                if (hit) {
                    DiscountContext ctx = new DiscountContext(
                            originalPrice, discount.getDiscountValue(), tag, userCrowdTag);
                    payPrice = discountCalculatorFactory.get(discount.getDiscountType()).calculate(ctx);
                    break;
                }
            }
        }
        context.setPayPrice(payPrice);
    }
}
