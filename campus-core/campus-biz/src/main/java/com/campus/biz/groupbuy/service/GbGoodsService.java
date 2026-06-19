package com.campus.biz.groupbuy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.biz.groupbuy.entity.GbGoods;
import com.campus.biz.groupbuy.entity.GbSku;

import java.math.BigDecimal;
import java.util.List;

/**
 * 拼团商品 Service（含 SKU 维护）
 */
public interface GbGoodsService extends IService<GbGoods> {

    /**
     * 后台分页（标题/状态/价格区间/库存预警筛选，按 sort 排序）
     */
    Page<GbGoods> adminPage(Integer page, Integer pageSize, String title, Integer status,
                            BigDecimal minPrice, BigDecimal maxPrice, Integer stockThreshold);

    /**
     * C 端分页（只查上架，含最低价，支持分类筛选与价格排序）
     */
    Page<GbGoods> webPage(Integer page, Integer pageSize, String title, String category, String sort);

    /**
     * 上架商品的去重分类列表
     */
    List<String> listCategories();

    /**
     * 新建/更新商品及其 SKU 列表（整单保存）
     */
    Long saveGoods(GbGoods goods, List<GbSku> skus);

    /**
     * 查询商品的 SKU 列表
     */
    List<GbSku> listSkus(Long goodsId);

    /**
     * 上架/下架
     */
    void changeStatus(Long id, Integer status);

    /**
     * 批量上架/下架
     */
    void changeStatusBatch(List<Long> ids, Integer status);

    /**
     * 删除商品（连带删除 SKU）
     */
    void removeGoods(Long id);
}
