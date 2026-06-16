package com.campus.biz.groupbuy.filter.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.biz.groupbuy.entity.GbOrder;
import com.campus.biz.groupbuy.entity.GbTeam;
import com.campus.biz.groupbuy.filter.SettlementContext;
import com.campus.biz.groupbuy.filter.SettlementFilter;
import com.campus.biz.groupbuy.mapper.GbOrderMapper;
import com.campus.biz.groupbuy.mapper.GbTeamMapper;
import com.campus.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 单号有效性：按 out_trade_no 查到拼单记录，校验状态为锁定，加载组队
 */
@Component
@RequiredArgsConstructor
public class OutTradeNoFilter implements SettlementFilter {

    private final GbOrderMapper orderMapper;
    private final GbTeamMapper teamMapper;

    private static final int ORDER_STATUS_LOCKED = 0;

    @Override
    public int order() {
        return 1;
    }

    @Override
    public void handle(SettlementContext context) {
        GbOrder order = orderMapper.selectOne(new LambdaQueryWrapper<GbOrder>()
                .eq(GbOrder::getOutTradeNo, context.getOutTradeNo()));
        if (order == null) {
            throw new BusinessException("拼单记录不存在: " + context.getOutTradeNo());
        }
        if (order.getStatus() != ORDER_STATUS_LOCKED) {
            throw new BusinessException("订单状态不允许结算，当前状态: " + order.getStatus());
        }
        GbTeam team = teamMapper.selectById(order.getTeamId());
        if (team == null) {
            throw new BusinessException("组队不存在: " + order.getTeamId());
        }
        context.setOrder(order);
        context.setTeam(team);
    }
}
