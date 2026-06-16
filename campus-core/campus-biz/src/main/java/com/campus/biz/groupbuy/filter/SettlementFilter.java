package com.campus.biz.groupbuy.filter;

/**
 * 结算责任链过滤器（借鉴小傅哥结算责任链：单号有效性→时间窗→成团判定→结算封装）
 */
public interface SettlementFilter {

    /**
     * 链中顺序，越小越先执行
     */
    int order();

    /**
     * 处理；不通过应抛 BusinessException 中断链路
     */
    void handle(SettlementContext context);
}
