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

import java.time.LocalDateTime;

/**
 * 成团进度：置当前订单为已支付，团 complete_count++，并判定是否达到成团目标
 */
@Component
@RequiredArgsConstructor
public class TeamProgressFilter implements SettlementFilter {

    private final GbOrderMapper orderMapper;
    private final GbTeamMapper teamMapper;

    private static final int ORDER_STATUS_PAID = 1;

    @Override
    public int order() {
        return 3;
    }

    @Override
    public void handle(SettlementContext context) {
        GbOrder order = context.getOrder();
        order.setStatus(ORDER_STATUS_PAID);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        GbTeam team = context.getTeam();
        // 精确自增 complete_count，避免整行覆盖
        teamMapper.update(null, new LambdaUpdateWrapper<GbTeam>()
                .eq(GbTeam::getId, team.getId())
                .setSql("complete_count = complete_count + 1"));
        int complete = (team.getCompleteCount() == null ? 0 : team.getCompleteCount()) + 1;
        team.setCompleteCount(complete);

        context.setTeamCompleted(complete >= team.getTargetCount());
    }
}
