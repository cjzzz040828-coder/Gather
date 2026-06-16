package com.campus.biz.groupbuy.trial.node;

import com.campus.biz.groupbuy.trial.AccessGuard;
import com.campus.biz.groupbuy.trial.TrialContext;
import com.campus.biz.groupbuy.trial.TrialNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 节点4·人群标签过滤：基于 Redis BitMap 校验活动可见性，并判定用户人群标签（供优惠计算匹配）。
 *
 * <p>排在优惠计算之前：先算出 userCrowdTag，优惠计算节点据此匹配人群专属折扣。</p>
 */
@Component
@RequiredArgsConstructor
public class CrowdTagNode implements TrialNode {

    private final AccessGuard accessGuard;

    @Override
    public int order() {
        return 40;
    }

    @Override
    public void handle(TrialContext context) {
        accessGuard.checkCrowdVisible(context.getActivity(), context.getUserId());
        context.setUserCrowdTag(accessGuard.userCrowdTag(context.getUserId()));
    }
}
