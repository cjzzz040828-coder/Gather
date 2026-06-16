package com.campus.biz.groupbuy.event;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.biz.groupbuy.entity.GbNotifyTask;
import com.campus.biz.groupbuy.mapper.GbNotifyTaskMapper;
import com.campus.biz.groupbuy.mq.GbNotifyProducer;
import com.campus.biz.groupbuy.notify.HttpNotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 成团事件监听器：事务提交后异步处理本地消息表。
 *
 * <p>成团 settle 在同一事务里写入 biz_gb_notify_task(status=0)，本监听器在事务提交后消费：</p>
 * <ul>
 *   <li>MQ 开启（GbNotifyProducer 存在）：把消息发到 RabbitMQ，由消费者异步处理 + 置成功；
 *       发送失败由定时补偿任务重投，本地消息表充当发送记录与补偿依据。</li>
 *   <li>MQ 关闭：回落本地直接消费，置消息为成功（保证无 broker 时链路仍闭环）。</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TeamCompletedListener {

    private final GbNotifyTaskMapper notifyTaskMapper;
    private final ObjectProvider<GbNotifyProducer> producerProvider;
    private final HttpNotifyService httpNotifyService;

    private static final int TASK_STATUS_PENDING = 0;
    private static final int TASK_STATUS_SUCCESS = 1;
    private static final int TASK_STATUS_FAIL = 2;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTeamCompleted(TeamCompletedEvent event) {
        log.info("成团事件触发: teamId={}, activityId={}", event.teamId(), event.activityId());
        List<GbNotifyTask> tasks = notifyTaskMapper.selectList(new LambdaQueryWrapper<GbNotifyTask>()
                .eq(GbNotifyTask::getBizId, event.teamId())
                .eq(GbNotifyTask::getMessageType, "team_complete")
                .eq(GbNotifyTask::getStatus, TASK_STATUS_PENDING));
        GbNotifyProducer producer = producerProvider.getIfAvailable();
        for (GbNotifyTask task : tasks) {
            try {
                if (producer != null) {
                    // MQ 主链：发到 RabbitMQ，由消费者置成功
                    producer.sendTeamComplete(task.getId());
                } else {
                    // 无 MQ：本地直接消费 + HTTP 回调外部商城
                    httpNotifyService.callback(task.getMessageType(), task.getMessageBody());
                    task.setStatus(TASK_STATUS_SUCCESS);
                    task.setUpdateTime(LocalDateTime.now());
                    notifyTaskMapper.updateById(task);
                }
            } catch (Exception e) {
                log.error("处理成团消息失败: taskId={}", task.getId(), e);
                task.setStatus(TASK_STATUS_FAIL);
                task.setRetryCount((task.getRetryCount() == null ? 0 : task.getRetryCount()) + 1);
                notifyTaskMapper.updateById(task);
            }
        }
    }
}
