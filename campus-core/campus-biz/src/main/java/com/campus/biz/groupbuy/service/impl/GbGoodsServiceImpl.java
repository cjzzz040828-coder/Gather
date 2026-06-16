package com.campus.biz.groupbuy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.biz.groupbuy.entity.GbGoods;
import com.campus.biz.groupbuy.entity.GbSku;
import com.campus.biz.groupbuy.mapper.GbGoodsMapper;
import com.campus.biz.groupbuy.mapper.GbSkuMapper;
import com.campus.biz.groupbuy.service.GbGoodsService;
import com.campus.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 拼团商品 Service 实现（含 SKU 维护）
 */
@Service
@RequiredArgsConstructor
public class GbGoodsServiceImpl extends ServiceImpl<GbGoodsMapper, GbGoods> implements GbGoodsService {

    private final GbSkuMapper skuMapper;

    /** 状态：下架 / 上架 */
    private static final int STATUS_OFFLINE = 0;
    private static final int STATUS_ONLINE = 1;

    @Override
    public Page<GbGoods> adminPage(Integer page, Integer pageSize, String title, Integer status) {
        LambdaQueryWrapper<GbGoods> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(title)) {
            wrapper.like(GbGoods::getTitle, title);
        }
        if (status != null) {
            wrapper.eq(GbGoods::getStatus, status);
        }
        wrapper.orderByDesc(GbGoods::getId);
        return this.page(new Page<>(page, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveGoods(GbGoods goods, List<GbSku> skus) {
        if (!StringUtils.hasText(goods.getTitle())) {
            throw new BusinessException("商品标题不能为空");
        }
        if (skus == null || skus.isEmpty()) {
            throw new BusinessException("至少需要一个 SKU");
        }
        if (goods.getStatus() == null) {
            goods.setStatus(STATUS_OFFLINE);
        }
        this.saveOrUpdate(goods);
        Long goodsId = goods.getId();

        // SKU 整单覆盖：删除原有再插入（编辑场景）
        skuMapper.delete(new LambdaQueryWrapper<GbSku>().eq(GbSku::getGoodsId, goodsId));
        for (GbSku sku : skus) {
            if (!StringUtils.hasText(sku.getSkuName())) {
                throw new BusinessException("SKU 名称不能为空");
            }
            sku.setId(null);
            sku.setGoodsId(goodsId);
            if (sku.getStatus() == null) {
                sku.setStatus(1);
            }
            if (sku.getStock() == null) {
                sku.setStock(0);
            }
            skuMapper.insert(sku);
        }
        return goodsId;
    }

    @Override
    public List<GbSku> listSkus(Long goodsId) {
        if (goodsId == null) {
            return new ArrayList<>();
        }
        return skuMapper.selectList(new LambdaQueryWrapper<GbSku>()
                .eq(GbSku::getGoodsId, goodsId)
                .orderByAsc(GbSku::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        GbGoods goods = this.getById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        if (status != STATUS_ONLINE && status != STATUS_OFFLINE) {
            throw new BusinessException("非法状态");
        }
        goods.setStatus(status);
        this.updateById(goods);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeGoods(Long id) {
        GbGoods goods = this.getById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        skuMapper.delete(new LambdaQueryWrapper<GbSku>().eq(GbSku::getGoodsId, id));
        this.removeById(id);
    }
}
