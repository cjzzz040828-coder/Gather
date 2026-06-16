package com.campus.biz.groupbuy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拼团商品
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_gb_goods")
public class GbGoods extends BaseEntity {

    /** 商品标题 */
    private String title;

    /** 封面图URL */
    private String cover;

    /** 轮播图URL(逗号分隔) */
    private String images;

    /** 图文详情(富文本) */
    private String detail;

    /** 商品分类 */
    private String category;

    /** 状态(0-下架 1-上架) */
    private Integer status;
}
