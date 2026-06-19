package com.campus.biz.groupbuy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 拼团商品 SKU（规格）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_gb_sku")
public class GbSku extends BaseEntity {

    /** 商品ID */
    private Long goodsId;

    /** 规格名称 */
    private String skuName;

    /** 规格图片URL */
    private String skuImage;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 库存 */
    private Integer stock;

    /** 状态(0-停用 1-启用) */
    private Integer status;
}
