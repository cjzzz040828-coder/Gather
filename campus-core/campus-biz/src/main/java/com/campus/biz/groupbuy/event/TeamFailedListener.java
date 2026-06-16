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
 * 成团失败监听器：超时收尾事务提交后异步处理 team_fail 本地消息。
 *
 * <p>与 {@link TeamCompletedListener} 对称：MQ 开启时发到 RabbitMQ 由消费者处理，
 * MQ 关闭时本地直接消费。本地消息表充当发送记录与定时补偿依据。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TeamFailedListener {

    private final GbNotifyTaskMapper notifyTaskMapper;
    private final ObjectProvider<GbNotifyProducer> producerProvider;
    private final HttpNotifyService httpNotifyService;

    private static final int TASK_STATUS_PENDING = 0;
    private static final int TASK_STATUS_SUCCESS = 1;
    private static final int TASK_STATUS_FAIL = 2;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTeamFailed(TeamFailedEvent event) {
        log.info("成团失败事件触发: teamId={}, activityId={}", event.teamId(), event.activityId());
        List<GbNotifyTask> tasks = notifyTaskMapper.selectList(new LambdaQueryWrapper<GbNotifyTask>()
                .eq(GbNotifyTask::getBizId, event.teamId())
                .eq(GbNotifyTask::getMessageType, "team_fail")
                .eq(GbNotifyTask::getStatus, TASK_STATUS_PENDING));
        GbNotifyProducer producer = producerProvider.getIfAvailable();
        for (GbNotifyTask task : tasks) {
            try {
                if (producer != null) {
                    producer.sendTeamFail(task.getId());
                } else {
                    httpNotifyService.callback(task.getMessageType(), task.getMessageBody());
                    task.setStatus(TASK_STATUS_SUCCESS);
                    task.setUpdateTime(LocalDateTime.now());
                    notifyTaskMapper.updateById(task);
                }
            } catch (Exception e) {
                log.error("处理成团失败消息失败: taskId={}", task.getId(), e);
                task.setStatus(TASK_STATUS_FAIL);
                task.setRetryCount((task.getRetryCount() == null ? 0 : task.getRetryCount()) + 1);
                notifyTaskMapper.updateById(task);
            }
        }
    }
}
