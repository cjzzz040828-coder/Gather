package com.campus.pay;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 模拟支付服务（无需真实渠道配置，下单即返回成功参数）
 *
 * <p>沿用 errand 模块"模拟支付直接置成功"的惯例，供拼团等业务在本地/沙箱跑通交易闭环。
 * 真实接入时改用 alipay/wechat 渠道并实现各自的 createPayOrder。</p>
 */
@Slf4j
@Service
public class MockPayService extends AbstractPayService {

    @Override
    public Map<String, String> createTestOrder() {
        return createPayOrder("MOCK" + IdUtil.getSnowflakeNextIdStr(), new BigDecimal("0.01"), "测试订单");
    }

    @Override
    public Map<String, String> createPayOrder(String outTradeNo, BigDecimal amount, String subject) {
        Map<String, String> result = new HashMap<>();
        result.put("outTradeNo", outTradeNo);
        result.put("orderNo", outTradeNo);
        result.put("amount", amount == null ? "0" : amount.toPlainString());
        result.put("subject", subject);
        String payUrl = "mock://pay?outTradeNo=" + outTradeNo + "&amount=" + result.get("amount");
        result.put("payUrl", payUrl);
        result.put("qrcode", generateQRCode(payUrl));
        log.info("模拟支付下单: outTradeNo={}, amount={}, subject={}", outTradeNo, result.get("amount"), subject);
        return result;
    }

    @Override
    public Map<String, String> refund(String outTradeNo, BigDecimal amount) {
        Map<String, String> result = new HashMap<>();
        result.put("outTradeNo", outTradeNo);
        result.put("refundAmount", amount == null ? "0" : amount.toPlainString());
        result.put("status", "SUCCESS");
        log.info("模拟支付退款: outTradeNo={}, refundAmount={}", outTradeNo, result.get("refundAmount"));
        return result;
    }

    @Override
    public String getPayType() {
        return "mock";
    }

    @Override
    public String getPayTypeName() {
        return "模拟支付";
    }
}
