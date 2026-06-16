package com.campus.biz.groupbuy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.biz.groupbuy.entity.GbActivity;
import com.campus.biz.groupbuy.entity.GbDiscount;

import java.util.List;

/**
 * 拼团活动 Service（含折扣维护）
 */
public interface GbActivityService extends IService<GbActivity> {

    /**
     * 后台分页（可按商品/状态筛选）
     */
    Page<GbActivity> adminPage(Integer page, Integer pageSize, Long goodsId, Integer status);

    /**
     * 查询某商品下进行中的活动列表（按 id 升序，供 PC 用户端商品详情页跳拼团）
     */
    List<GbActivity> listRunningByGoodsId(Long goodsId);

    /**
     * 新建/更新活动及其折扣列表（整单保存）
     */
    Long saveActivity(GbActivity activity, List<GbDiscount> discounts);

    /**
     * 查询活动的折扣列表（按优先级降序）
     */
    List<GbDiscount> listDiscounts(Long activityId);

    /**
     * 更新活动状态
     */
    void changeStatus(Long id, Integer status);

    /**
     * 删除活动（连带删除折扣）
     */
    void removeActivity(Long id);
}
