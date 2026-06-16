package com.campus.biz.groupbuy.trial;

/**
 * 试算规则树节点（规则树式优惠试算链路）。
 *
 * <p>各节点按 order 升序串联：参数校验 → 动态开关 → 营销配置加载 → 优惠计算
 * → 人群标签过滤 → 异常兜底。借鉴小傅哥试算责任链，节点解耦、可插拔扩展。</p>
 */
public interface TrialNode {

    /**
     * 链中顺序，越小越先执行
     */
    int order();

    /**
     * 处理当前节点；不通过应抛 BusinessException 中断链路
     */
    void handle(TrialContext context);
}
