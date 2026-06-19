package com.campus.biz.groupbuy.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拼团商品
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_gb_goods")
public class GbGoods extends BaseEntity {

    /** 商品标题 */
    private String title;

    /** 副标题 */
    private String subtitle;

    /** 封面图URL */
    private String cover;

    /** 轮播图URL(逗号分隔) */
    private String images;

    /** 图文详情(富文本) */
    private String detail;

    /** 划线价/市场价 */
    private BigDecimal marketPrice;

    /** 销量 */
    private Integer sales;

    /** 排序权重(大在前) */
    private Integer sort;

    /** 上架时间 */
    private LocalDateTime publishTime;

    /** 商品分类 */
    private String category;

    /** 状态(0-下架 1-上架) */
    private Integer status;

    /** 最低 SKU 价（非数据库字段，列表查询时 join 计算，用于展示与排序） */
    @TableField(exist = false)
    private BigDecimal minPrice;
}
