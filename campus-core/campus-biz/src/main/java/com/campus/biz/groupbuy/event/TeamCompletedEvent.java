package com.campus.biz.groupbuy.event;

/**
 * 成团事件：拼团达成成团目标后发布，由监听器异步处理后续（通知/状态扭转）
 */
public record TeamCompletedEvent(Long teamId, Long activityId) {
}
