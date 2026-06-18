package com.campus.biz.groupbuy.trial.node;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.biz.groupbuy.entity.GbActivity;
import com.campus.biz.groupbuy.entity.GbDiscount;
import com.campus.biz.groupbuy.entity.GbSku;
import com.campus.biz.groupbuy.mapper.GbActivityMapper;
import com.campus.biz.groupbuy.mapper.GbDiscountMapper;
import com.campus.biz.groupbuy.mapper.GbSkuMapper;
import com.campus.biz.groupbuy.trial.TrialContext;
import com.campus.biz.groupbuy.trial.TrialNode;
import com.campus.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 节点3·营销配置加载：加载活动、SKU、折扣规则，并校验活动状态与时间窗，填入上下文。
 */
@Component
@RequiredArgsConstructor
public class MarketLoadNode implements TrialNode {

    private final GbActivityMapper activityMapper;
    private final GbSkuMapper skuMapper;
    private final GbDiscountMapper discountMapper;

    private static final int ACTIVITY_STATUS_RUNNING = 1;

    @Override
    public int order() {
        return 30;
    }

    @Override
    public void handle(TrialContext context) {
        GbActivity activity = activityMapper.selectById(context.getActivityId());
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        if (activity.getStatus() != ACTIVITY_STATUS_RUNNING) {
            throw new BusinessException("活动未在进行中");
        }
        LocalDateTime now = LocalDateTime.now();
        if (activity.getValidStart() != null && now.isBefore(activity.getValidStart())) {
            throw new BusinessException("活动尚未开始");
        }
        if (activity.getValidEnd() != null && now.isAfter(activity.getValidEnd())) {
            throw new BusinessException("活动已结束");
        }

        GbSku sku = resolveSku(context.getSkuId(), activity);

        List<GbDiscount> discounts = discountMapper.selectList(new LambdaQueryWrapper<GbDiscount>()
                .eq(GbDiscount::getActivityId, activity.getId())
                .orderByDesc(GbDiscount::getPriority));

        context.setActivity(activity);
        context.setSku(sku);
        context.setDiscounts(discounts);
        context.setOriginalPrice(sku.getOriginalPrice());
    }

    /**
     * 解析下单/试算 SKU：入参 skuId 优先，为空回落活动默认 SKU；
     * 校验该 SKU 属于活动商品且为启用状态，防止跨商品或停用 SKU。
     */
    private GbSku resolveSku(Long inputSkuId, GbActivity activity) {
        Long skuId = inputSkuId != null ? inputSkuId : activity.getSkuId();
        if (skuId == null) {
            throw new BusinessException("请选择商品规格");
        }
        GbSku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException("SKU 不存在");
        }
        if (!activity.getGoodsId().equals(sku.getGoodsId())) {
            throw new BusinessException("所选规格不属于该商品");
        }
        if (sku.getStatus() != null && sku.getStatus() != 1) {
            throw new BusinessException("该规格已下架");
        }
        return sku;
    }
}
