package com.campus.pay;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付服务接口
 */
public interface PayService {

    /**
     * 创建测试订单（Native扫码支付）
     * @return 包含 orderNo, qrcode, payUrl 等信息
     */
    Map<String, String> createTestOrder();

    /**
     * 按金额创建支付订单
     * @param outTradeNo 业务侧外部交易单号
     * @param amount     金额（元）
     * @param subject    订单标题
     * @return 包含 outTradeNo, qrcode, payUrl 等信息
     */
    default Map<String, String> createPayOrder(String outTradeNo, BigDecimal amount, String subject) {
        throw new UnsupportedOperationException("当前支付方式未实现按金额下单: " + getPayType());
    }

    /**
     * 按外部交易单号退款
     * @param outTradeNo 业务侧外部交易单号
     * @param amount     退款金额（元）
     * @return 包含 outTradeNo, refundAmount, status 等信息
     */
    default Map<String, String> refund(String outTradeNo, BigDecimal amount) {
        throw new UnsupportedOperationException("当前支付方式未实现退款: " + getPayType());
    }

    /**
     * 获取支付方式名称
     */
    String getPayType();

    /**
     * 获取支付方式显示名称
     */
    String getPayTypeName();
}
