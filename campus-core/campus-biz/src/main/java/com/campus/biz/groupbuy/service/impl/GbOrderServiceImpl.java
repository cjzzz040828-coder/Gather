package com.campus.biz.groupbuy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.biz.groupbuy.entity.GbOrder;
import com.campus.biz.groupbuy.mapper.GbOrderMapper;
import com.campus.biz.groupbuy.service.GbOrderService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 拼单记录 Service 实现
 */
@Service
public class GbOrderServiceImpl extends ServiceImpl<GbOrderMapper, GbOrder> implements GbOrderService {

    @Override
    public Page<GbOrder> adminPage(Integer page, Integer pageSize, Long teamId, Long userId, Integer status) {
        LambdaQueryWrapper<GbOrder> wrapper = new LambdaQueryWrapper<>();
        if (teamId != null) {
            wrapper.eq(GbOrder::getTeamId, teamId);
        }
        if (userId != null) {
            wrapper.eq(GbOrder::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(GbOrder::getStatus, status);
        }
        wrapper.orderByDesc(GbOrder::getId);
        return this.page(new Page<>(page, pageSize), wrapper);
    }

    @Override
    public List<GbOrder> listMine(Long userId) {
        return this.list(new LambdaQueryWrapper<GbOrder>()
                .eq(GbOrder::getUserId, userId)
                .orderByDesc(GbOrder::getId));
    }

    @Override
    public GbOrder getMineOrder(Long id, Long userId) {
        GbOrder order = this.getById(id);
        if (order == null || !userId.equals(order.getUserId())) {
            return null;
        }
        return order;
    }
}
