package com.campus.biz.groupbuy.refund;

import com.campus.biz.groupbuy.entity.GbOrder;
import com.campus.biz.groupbuy.mapper.GbOrderMapper;
import com.campus.biz.groupbuy.mapper.GbSkuMapper;
import com.campus.biz.groupbuy.mapper.GbTeamMapper;
import com.campus.pay.PayServiceFactory;
import lombok.Data;

/**
 * 退单上下文：枚举策略常量非 Spring bean，依赖句柄由调用方（service）注入。
 */
@Data
public class RefundContext {

    /** 待退订单 */
    private final GbOrder order;

    private final GbOrderMapper orderMapper;
    private final GbSkuMapper skuMapper;
    private final GbTeamMapper teamMapper;
    private final PayServiceFactory payServiceFactory;
}
