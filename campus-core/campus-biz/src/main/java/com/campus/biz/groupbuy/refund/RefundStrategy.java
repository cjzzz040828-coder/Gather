package com.campus.biz.groupbuy.refund;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campus.biz.groupbuy.entity.GbOrder;
import com.campus.biz.groupbuy.entity.GbSku;
import com.campus.biz.groupbuy.entity.GbTeam;
import lombok.extern.slf4j.Slf4j;

/**
 * 退单枚举策略：按订单当前状态分流退单动作（行为内聚到枚举常量）。
 *
 * <p>超时未成团收尾时，对团内每个订单调用 {@link #from(GbOrder)} 取对应策略再 {@link #refund(RefundContext)}。
 * 与成团结算（SettlementEndFilter）对称——成团把已付订单推进到「成团」，失败则把订单退回并归还库存。</p>
 */
@Slf4j
public enum RefundStrategy {

    /** 锁定未支付：无款可退，仅归还库存 + 团锁单数 -1。 */
    LOCKED {
        @Override
        public void refund(RefundContext ctx) {
            restoreStockAndTeam(ctx);
            markRefunded(ctx);
            log.info("退单(锁定未支付): orderId={}, outTradeNo={}", ctx.getOrder().getId(), ctx.getOrder().getOutTradeNo());
        }
    },

    /** 已支付：调支付退款 + 归还库存 + 团锁单数 -1。 */
    PAID {
        @Override
        public void refund(RefundContext ctx) {
            GbOrder order = ctx.getOrder();
            ctx.getPayServiceFactory().refund(order.getOutTradeNo(), order.getPayAmount());
            restoreStockAndTeam(ctx);
            markRefunded(ctx);
            log.info("退单(已支付退款): orderId={}, outTradeNo={}, amount={}",
                    order.getId(), order.getOutTradeNo(), order.getPayAmount());
        }
    },

    /** 不退（已成团/已退款等，防御性空操作）。 */
    NONE {
        @Override
        public void refund(RefundContext ctx) {
            log.warn("退单跳过(状态不可退): orderId={}, status={}", ctx.getOrder().getId(), ctx.getOrder().getStatus());
        }
    };

    private static final int ORDER_STATUS_LOCKED = 0;
    private static final int ORDER_STATUS_PAID = 1;
    private static final int ORDER_STATUS_REFUNDED = 3;

    public abstract void refund(RefundContext ctx);

    /** 按订单状态选取退单策略。 */
    public static RefundStrategy from(GbOrder order) {
        return switch (order.getStatus()) {
            case ORDER_STATUS_LOCKED -> LOCKED;
            case ORDER_STATUS_PAID -> PAID;
            default -> NONE;
        };
    }

    /** 归还该订单占用的 1 件库存，并把团锁单数 -1；已付单同时把已支付人数 -1（与支付时 complete_count++ 对称）。 */
    protected static void restoreStockAndTeam(RefundContext ctx) {
        GbOrder order = ctx.getOrder();
        ctx.getSkuMapper().update(null, new LambdaUpdateWrapper<GbSku>()
                .eq(GbSku::getId, order.getSkuId())
                .setSql("stock = stock + 1"));

        LambdaUpdateWrapper<GbTeam> teamUpdate = new LambdaUpdateWrapper<GbTeam>()
                .eq(GbTeam::getId, order.getTeamId())
                .gt(GbTeam::getLockCount, 0)
                .setSql("lock_count = lock_count - 1");
        // 已支付订单退款：已支付人数也要回退（支付结算时 complete_count 加过）
        if (order.getStatus() != null && order.getStatus() == ORDER_STATUS_PAID) {
            teamUpdate.gt(GbTeam::getCompleteCount, 0)
                    .setSql("complete_count = complete_count - 1");
        }
        ctx.getTeamMapper().update(null, teamUpdate);
    }

    /** 订单置为已退款。 */
    protected static void markRefunded(RefundContext ctx) {
        ctx.getOrder().setStatus(ORDER_STATUS_REFUNDED);
        ctx.getOrderMapper().updateById(ctx.getOrder());
    }
}
