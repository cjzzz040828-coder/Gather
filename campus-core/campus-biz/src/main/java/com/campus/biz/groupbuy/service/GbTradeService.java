package com.campus.biz.groupbuy.service;

import com.campus.biz.groupbuy.dto.LockOrderDTO;
import com.campus.biz.groupbuy.dto.LockOrderResultDTO;
import com.campus.biz.groupbuy.dto.TrialResultDTO;

/**
 * 拼团核心交易 Service：试算 → 锁单 → 支付回调结算
 */
public interface GbTradeService {

    /**
     * 试算：按活动折扣策略算出优惠后实付价（含人群标签命中）
     */
    TrialResultDTO trial(Long activityId, Long userId);

    /**
     * 锁单：校验活动/库存 → 建团或加入团 → 写拼单记录(锁定) → 调支付下单
     */
    LockOrderResultDTO lockOrder(LockOrderDTO dto, Long userId);

    /**
     * 支付成功回调：走结算责任链，成团则发布事件 + 写本地消息表
     */
    void payCallback(String outTradeNo);

    /**
     * 收尾超时未成团的拼团：逐团置失败 + 按订单状态退单（退款/释放库存）+ 写 team_fail 本地消息。
     * @return 本轮处理的团数
     */
    int closeTimeoutTeams();
}
