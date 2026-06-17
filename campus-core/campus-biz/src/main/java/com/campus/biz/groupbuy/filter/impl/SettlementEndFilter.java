package com.campus.biz.groupbuy.filter.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campus.biz.groupbuy.entity.GbOrder;
import com.campus.biz.groupbuy.entity.GbTeam;
import com.campus.biz.groupbuy.filter.SettlementContext;
import com.campus.biz.groupbuy.filter.SettlementFilter;
import com.campus.biz.groupbuy.mapper.GbOrderMapper;
import com.campus.biz.groupbuy.mapper.GbTeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 结算封装：若达成成团，置组队为成功，并将团内所有已支付订单置为成团
 */
@Component
@RequiredArgsConstructor
public class SettlementEndFilter implements SettlementFilter {

    private final GbOrderMapper orderMapper;
    private final GbTeamMapper teamMapper;

    private static final int ORDER_STATUS_PAID = 1;
    private static final int ORDER_STATUS_GROUPED = 2;
    private static final int TEAM_STATUS_SUCCESS = 1;

    @Override
    public int order() {
        return 4;
    }

    @Override
    public void handle(SettlementContext context) {
        if (!context.isTeamCompleted()) {
            return;
        }
        GbTeam team = context.getTeam();
        // 精确只更新 status，避免 updateById 用旧 team 对象整行覆盖其它字段（曾导致成团团被旧状态覆盖）
        teamMapper.update(null, new LambdaUpdateWrapper<GbTeam>()
                .eq(GbTeam::getId, team.getId())
                .set(GbTeam::getStatus, TEAM_STATUS_SUCCESS));
        team.setStatus(TEAM_STATUS_SUCCESS);

        // 团内全部已支付订单 → 成团
        orderMapper.update(null, new LambdaUpdateWrapper<GbOrder>()
                .eq(GbOrder::getTeamId, team.getId())
                .eq(GbOrder::getStatus, ORDER_STATUS_PAID)
                .set(GbOrder::getStatus, ORDER_STATUS_GROUPED));
    }
}
