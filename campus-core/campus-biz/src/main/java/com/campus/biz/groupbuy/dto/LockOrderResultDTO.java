package com.campus.biz.groupbuy.dto;

import lombok.Data;

import java.util.Map;

/**
 * 锁单结果：拼单记录ID + 组队ID + 外部交易单号 + 支付参数
 */
@Data
public class LockOrderResultDTO {

    /** 拼单记录ID */
    private Long orderId;

    /** 组队ID */
    private Long teamId;

    /** 外部交易单号 */
    private String outTradeNo;

    /** 支付参数（qrcode/payUrl 等） */
    private Map<String, String> payParams;
}
