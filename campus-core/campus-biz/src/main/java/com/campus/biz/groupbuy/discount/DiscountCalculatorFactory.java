package com.campus.biz.groupbuy.discount;

import com.campus.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 折扣策略工厂：按 discountType 分发到对应 DiscountCalculator
 */
@Component
@RequiredArgsConstructor
public class DiscountCalculatorFactory {

    private final List<DiscountCalculator> calculators;
    private final Map<Integer, DiscountCalculator> calculatorMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (DiscountCalculator calculator : calculators) {
            calculatorMap.put(calculator.getDiscountType(), calculator);
        }
    }

    public DiscountCalculator get(Integer discountType) {
        DiscountCalculator calculator = calculatorMap.get(discountType);
        if (calculator == null) {
            throw new BusinessException("不支持的折扣类型: " + discountType);
        }
        return calculator;
    }
}
