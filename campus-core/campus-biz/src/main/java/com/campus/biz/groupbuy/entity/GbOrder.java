package com.campus.biz.groupbuy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拼单记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_gb_order")
public class GbOrder extends BaseEntity {

    /** 组队ID */
    private Long teamId;

    /** 活动ID */
    private Long activityId;

    /** SKU ID */
    private Long skuId;

    /** 购买数量 */
    private Integer quantity;

    /** 拼单用户ID */
    private Long userId;

    /** 外部交易单号(唯一) */
    private String outTradeNo;

    /** 原始金额 */
    private BigDecimal originalAmount;

    /** 优惠金额 */
    private BigDecimal deductionAmount;

    /** 实付金额 */
    private BigDecimal payAmount;

    /** 状态(0-锁定 1-已支付 2-成团 3-退款) */
    private Integer status;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 收货人快照 */
    private String receiver;

    /** 电话快照 */
    private String phone;

    /** 收货地址快照 */
    private String address;

    /** 退款原因 */
    private String refundReason;
}
