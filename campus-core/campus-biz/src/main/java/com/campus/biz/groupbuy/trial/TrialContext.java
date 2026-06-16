package com.campus.biz.groupbuy.trial;

import com.campus.biz.groupbuy.entity.GbActivity;
import com.campus.biz.groupbuy.entity.GbDiscount;
import com.campus.biz.groupbuy.entity.GbSku;
import com.campus.biz.groupbuy.dto.TrialResultDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 试算规则树上下文：贯穿各节点，承载入参、加载的营销配置、中间计算态与最终结果。
 *
 * <p>节点之间不直接调用，只通过本上下文传递数据，降低耦合。任一节点不通过应抛
 * BusinessException 中断链路，由兜底节点统一兜住异常语义。</p>
 */
@Data
public class TrialContext {

    // ===== 入参 =====
    private Long activityId;
    private Long userId;

    // ===== 营销配置加载节点填充 =====
    private GbActivity activity;
    private GbSku sku;
    private List<GbDiscount> discounts;

    // ===== 人群标签节点填充 =====
    private String userCrowdTag;

    // ===== 优惠计算节点填充 =====
    private BigDecimal originalPrice;
    private BigDecimal payPrice;

    // ===== 最终结果 =====
    private TrialResultDTO result;
}
