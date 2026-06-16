package com.campus.biz.groupbuy.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.biz.groupbuy.entity.GbNotifyTask;
import com.campus.biz.groupbuy.mapper.GbNotifyTaskMapper;
import com.campus.biz.groupbuy.mq.GbNotifyProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 拼团通知补偿定时任务：扫描迟迟未投递成功的本地消息表，重投 MQ（或本地兜底处理）。
 *
 * <p>保障最终一致性：成团/失败时消息先入库（与交易同事务），监听器发 MQ。
 * 若 MQ 发送失败或消费异常，消息会停留在 PENDING；本任务周期性捞出超过宽限期仍 PENDING 的消息：</p>
 * <ul>
 *   <li>MQ 开启：重投到 MQ，由消费者最终处理并置成功。</li>
 *   <li>MQ 关闭：本地直接置成功（无 broker 时兜底）。</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GbNotifyCompensateTask {

    private final GbNotifyTaskMapper notifyTaskMapper;
    private final ObjectProvider<GbNotifyProducer> producerProvider;

    private static final int TASK_STATUS_PENDING = 0;
    private static final int TASK_STATUS_SUCCESS = 1;

    /** 消息入库后超过该秒数仍未成功才补偿，避免与正常投递/消费竞争。 */
    private static final long GRACE_SECONDS = 30;

    @Scheduled(fixedDelayString = "${groupbuy.notify-compensate.delay-ms:60000}")
    public void compensate() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(GRACE_SECONDS);
        List<GbNotifyTask> pending = notifyTaskMapper.selectList(new LambdaQueryWrapper<GbNotifyTask>()
                .eq(GbNotifyTask::getStatus, TASK_STATUS_PENDING)
                .lt(GbNotifyTask::getCreateTime, threshold));
        if (pending.isEmpty()) {
            return;
        }
        GbNotifyProducer producer = producerProvider.getIfAvailable();
        int handled = 0;
        for (GbNotifyTask task : pending) {
            try {
                if (producer != null) {
                    if ("team_fail".equals(task.getMessageType())) {
                        producer.sendTeamFail(task.getId());
                    } else {
                        producer.sendTeamComplete(task.getId());
                    }
                } else {
                    task.setStatus(TASK_STATUS_SUCCESS);
                    task.setUpdateTime(LocalDateTime.now());
                    notifyTaskMapper.updateById(task);
                }
                task.setRetryCount((task.getRetryCount() == null ? 0 : task.getRetryCount()) + 1);
                notifyTaskMapper.updateById(task);
                handled++;
            } catch (Exception e) {
                log.error("通知补偿失败: taskId={}", task.getId(), e);
            }
        }
        log.info("拼团通知补偿: 处理 {} 条滞留消息", handled);
    }
}
