package com.campus.biz.groupbuy.trial.node;

import com.campus.biz.groupbuy.dto.TrialResultDTO;
import com.campus.biz.groupbuy.trial.TrialContext;
import com.campus.biz.groupbuy.trial.TrialNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 节点6·异常兜底：组装最终试算结果，并对计算结果做完整性防御。
 *
 * <p>若上游算出的实付价异常（为空 / 为负 / 高于原价），兜底回退为原价，
 * 避免把异常价格透出给下游下单链路；前序节点未填充的字段在此补齐。</p>
 */
@Slf4j
@Component
public class FallbackNode implements TrialNode {

    @Override
    public int order() {
        return 60;
    }

    @Override
    public void handle(TrialContext context) {
        BigDecimal originalPrice = context.getOriginalPrice();
        BigDecimal payPrice = context.getPayPrice();

        // 结果完整性兜底：实付价异常时回退原价
        if (payPrice == null
                || payPrice.compareTo(BigDecimal.ZERO) < 0
                || payPrice.compareTo(originalPrice) > 0) {
            log.warn("试算实付价异常，兜底回退原价: activityId={}, payPrice={}, originalPrice={}",
                    context.getActivityId(), payPrice, originalPrice);
            payPrice = originalPrice;
            context.setPayPrice(payPrice);
        }

        TrialResultDTO result = new TrialResultDTO();
        result.setActivityId(context.getActivityId());
        result.setSkuId(context.getSku().getId());
        result.setOriginalPrice(originalPrice);
        result.setPayPrice(payPrice);
        result.setDeductionAmount(originalPrice.subtract(payPrice));
        result.setTargetCount(context.getActivity().getTargetCount());
        context.setResult(result);
    }
}
