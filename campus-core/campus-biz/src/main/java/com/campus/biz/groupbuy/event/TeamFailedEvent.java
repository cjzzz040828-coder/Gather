package com.campus.biz.groupbuy.event;

/**
 * 成团失败事件：拼团超时未达成团目标、收尾退款后发布，由监听器异步处理后续（通知退款到账等）
 */
public record TeamFailedEvent(Long teamId, Long activityId) {
}
