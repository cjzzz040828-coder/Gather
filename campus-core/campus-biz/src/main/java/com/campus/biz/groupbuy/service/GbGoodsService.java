package com.campus.biz.groupbuy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.biz.groupbuy.entity.GbGoods;
import com.campus.biz.groupbuy.entity.GbSku;

import java.util.List;

/**
 * 拼团商品 Service（含 SKU 维护）
 */
public interface GbGoodsService extends IService<GbGoods> {

    /**
     * 后台分页（可按标题/状态筛选）
     */
    Page<GbGoods> adminPage(Integer page, Integer pageSize, String title, Integer status);

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
     * 删除商品（连带删除 SKU）
     */
    void removeGoods(Long id);
}
