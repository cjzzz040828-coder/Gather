package com.campus.biz.groupbuy.trial.node;

import com.campus.biz.groupbuy.trial.AccessGuard;
import com.campus.biz.groupbuy.trial.TrialContext;
import com.campus.biz.groupbuy.trial.TrialNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 节点2·动态开关：DCC 运行期开关校验（活动降级 / 用户切量 / 黑名单）。
 */
@Component
@RequiredArgsConstructor
public class DynamicSwitchNode implements TrialNode {

    private final AccessGuard accessGuard;

    @Override
    public int order() {
        return 20;
    }

    @Override
    public void handle(TrialContext context) {
        accessGuard.checkDynamicSwitch(context.getUserId());
    }
}
