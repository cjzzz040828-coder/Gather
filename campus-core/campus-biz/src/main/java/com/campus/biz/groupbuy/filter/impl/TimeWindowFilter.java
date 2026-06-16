package com.campus.biz.groupbuy.filter.impl;

import com.campus.biz.groupbuy.entity.GbTeam;
import com.campus.biz.groupbuy.filter.SettlementContext;
import com.campus.biz.groupbuy.filter.SettlementFilter;
import com.campus.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 时间窗：组队须处于拼团中且未超过本团截止时间
 */
@Component
public class TimeWindowFilter implements SettlementFilter {

    private static final int TEAM_STATUS_GROUPING = 0;

    @Override
    public int order() {
        return 2;
    }

    @Override
    public void handle(SettlementContext context) {
        GbTeam team = context.getTeam();
        if (team.getStatus() != TEAM_STATUS_GROUPING) {
            throw new BusinessException("组队已结束，无法结算");
        }
        if (team.getValidEndTime() != null && LocalDateTime.now().isAfter(team.getValidEndTime())) {
            throw new BusinessException("拼团已超时，无法结算");
        }
    }
}
