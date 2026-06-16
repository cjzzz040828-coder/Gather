package com.campus.biz.groupbuy.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 拼团通知生产者：把本地消息表的 taskId 发到对应队列，由消费者异步处理。
 *
 * <p>仅当 groupbuy.mq.enabled=true 时装配。消息只携带 taskId（本地消息表主键），
 * 消费端据此查表处理，做到幂等且不依赖消息体的完整性。</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "groupbuy.mq.enabled", havingValue = "true")
public class GbNotifyProducer {

    private final RabbitTemplate rabbitTemplate;

    public GbNotifyProducer(@Qualifier("gbRabbitTemplate") RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendTeamComplete(Long taskId) {
        rabbitTemplate.convertAndSend(GbMqConfig.EXCHANGE, GbMqConfig.RK_TEAM_COMPLETE, taskId);
        log.info("MQ 发送成团通知: taskId={}", taskId);
    }

    public void sendTeamFail(Long taskId) {
        rabbitTemplate.convertAndSend(GbMqConfig.EXCHANGE, GbMqConfig.RK_TEAM_FAIL, taskId);
        log.info("MQ 发送失败通知: taskId={}", taskId);
    }
}
