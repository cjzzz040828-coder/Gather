package com.campus.biz.groupbuy.mq;

import com.campus.biz.groupbuy.entity.GbNotifyTask;
import com.campus.biz.groupbuy.mapper.GbNotifyTaskMapper;
import com.campus.biz.groupbuy.notify.HttpNotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 拼团通知消费者：从 MQ 收 taskId，查本地消息表执行后续动作并置成功。
 *
 * <p>仅当 groupbuy.mq.enabled=true 时装配。消费幂等：已成功的 task 直接跳过，
 * 避免 MQ 重投或与定时补偿并发导致的重复处理。</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "groupbuy.mq.enabled", havingValue = "true")
@RequiredArgsConstructor
public class GbNotifyConsumer {

    private final GbNotifyTaskMapper notifyTaskMapper;
    private final HttpNotifyService httpNotifyService;

    private static final int TASK_STATUS_SUCCESS = 1;

    @RabbitListener(queues = GbMqConfig.QUEUE_TEAM_COMPLETE)
    public void onTeamComplete(Long taskId) {
        handle(taskId, "team_complete");
    }

    @RabbitListener(queues = GbMqConfig.QUEUE_TEAM_FAIL)
    public void onTeamFail(Long taskId) {
        handle(taskId, "team_fail");
    }

    private void handle(Long taskId, String type) {
        GbNotifyTask task = notifyTaskMapper.selectById(taskId);
        if (task == null) {
            log.warn("MQ 消费忽略：本地消息不存在, taskId={}", taskId);
            return;
        }
        if (task.getStatus() != null && task.getStatus() == TASK_STATUS_SUCCESS) {
            return; // 幂等：已处理
        }
        // HTTP 回调外部商城，完成拼团营销闭环（未配置回调地址则跳过）
        httpNotifyService.callback(task.getMessageType(), task.getMessageBody());
        task.setStatus(TASK_STATUS_SUCCESS);
        task.setUpdateTime(LocalDateTime.now());
        notifyTaskMapper.updateById(task);
        log.info("MQ 消费完成[{}]: taskId={}, bizId={}", type, taskId, task.getBizId());
    }
}
