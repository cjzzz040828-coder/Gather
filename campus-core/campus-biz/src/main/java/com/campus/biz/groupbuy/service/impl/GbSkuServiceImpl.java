package com.campus.biz.groupbuy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.biz.groupbuy.entity.GbSku;
import com.campus.biz.groupbuy.mapper.GbSkuMapper;
import com.campus.biz.groupbuy.service.GbSkuService;
import org.springframework.stereotype.Service;

/**
 * 拼团商品 SKU Service 实现
 */
@Service
public class GbSkuServiceImpl extends ServiceImpl<GbSkuMapper, GbSku> implements GbSkuService {
}
