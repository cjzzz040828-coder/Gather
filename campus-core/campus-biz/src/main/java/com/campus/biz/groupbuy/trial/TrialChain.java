package com.campus.biz.groupbuy.trial;

import com.campus.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;

/**
 * 试算规则树编排：按 order 升序依次执行各节点。
 *
 * <p>外层统一兜住非业务异常，转为可读的业务错误，避免底层异常细节裸露给调用方；
 * 业务校验类中断（BusinessException）则原样上抛。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrialChain {

    private final List<TrialNode> nodes;
    private List<TrialNode> orderedNodes;

    @PostConstruct
    public void init() {
        orderedNodes = nodes.stream()
                .sorted(Comparator.comparingInt(TrialNode::order))
                .toList();
    }

    /**
     * 执行试算规则树，返回携带结果的上下文
     */
    public TrialContext execute(Long activityId, Long skuId, Integer quantity, Long userId) {
        TrialContext context = new TrialContext();
        context.setActivityId(activityId);
        context.setSkuId(skuId);
        context.setQuantity(quantity);
        context.setUserId(userId);
        try {
            for (TrialNode node : orderedNodes) {
                node.handle(context);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("试算链路异常: activityId={}, userId={}", activityId, userId, e);
            throw new BusinessException("试算失败，请稍后重试");
        }
        return context;
    }
}
