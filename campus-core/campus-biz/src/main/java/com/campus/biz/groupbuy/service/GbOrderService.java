package com.campus.biz.groupbuy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.biz.groupbuy.entity.GbOrder;

import java.util.List;

/**
 * 拼单记录 Service
 */
public interface GbOrderService extends IService<GbOrder> {

    /**
     * 后台分页（可按组队/用户/状态筛选）
     */
    Page<GbOrder> adminPage(Integer page, Integer pageSize, Long teamId, Long userId, Integer status);

    /**
     * 我的拼单（按用户，倒序）
     */
    List<GbOrder> listMine(Long userId);

    /**
     * 取我的某笔订单（校验归属，越权/不存在返回 null）
     */
    GbOrder getMineOrder(Long id, Long userId);
}
