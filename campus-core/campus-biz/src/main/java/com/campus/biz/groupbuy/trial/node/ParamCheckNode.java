package com.campus.biz.groupbuy.trial.node;

import com.campus.biz.groupbuy.trial.TrialContext;
import com.campus.biz.groupbuy.trial.TrialNode;
import com.campus.common.exception.BusinessException;
import org.springframework.stereotype.Component;

/**
 * 节点1·参数校验：校验试算入参完整性。
 */
@Component
public class ParamCheckNode implements TrialNode {

    @Override
    public int order() {
        return 10;
    }

    @Override
    public void handle(TrialContext context) {
        if (context.getActivityId() == null) {
            throw new BusinessException("活动ID不能为空");
        }
        if (context.getUserId() == null) {
            throw new BusinessException("用户未登录");
        }
    }
}
