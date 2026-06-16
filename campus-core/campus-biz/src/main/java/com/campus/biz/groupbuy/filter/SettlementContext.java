package com.campus.biz.groupbuy.filter;

import com.campus.biz.groupbuy.entity.GbOrder;
import com.campus.biz.groupbuy.entity.GbTeam;
import lombok.Data;

/**
 * 结算上下文：在责任链中传递的支付回调数据
 */
@Data
public class SettlementContext {

    /** 外部交易单号 */
    private String outTradeNo;

    /** 命中的拼单记录（由 OutTradeNoFilter 填充） */
    private GbOrder order;

    /** 命中的组队（由 OutTradeNoFilter 填充） */
    private GbTeam team;

    /** 本次回调是否触发成团（由 TeamProgressFilter 判定） */
    private boolean teamCompleted;
}
