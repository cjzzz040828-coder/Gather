package com.campus.biz.groupbuy.dto;

import lombok.Data;

/**
 * 锁单请求
 */
@Data
public class LockOrderDTO {

    /** 活动ID */
    private Long activityId;

    /** SKU ID（为空则回落活动默认 SKU） */
    private Long skuId;

    /** 购买数量（为空按 1 件） */
    private Integer quantity;

    /** 加入的组队ID（为空则新建团并成为团长） */
    private Long teamId;

    /** 收货地址ID（实物商品下单必填） */
    private Long addressId;
}
