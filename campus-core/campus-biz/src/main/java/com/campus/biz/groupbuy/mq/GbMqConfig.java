package com.campus.biz.groupbuy.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 拼团通知 MQ 拓扑：成团/失败两类异步通知走 RabbitMQ。
 *
 * <p>仅当 groupbuy.mq.enabled=true 时装配，本机无 broker 时默认关闭、不影响启动。
 * 通知消息在交易事务内先落本地消息表 biz_gb_notify_task，事务提交后发到 MQ；
 * 定时补偿任务扫描未投递成功的消息重投，保障最终一致性。</p>
 */
@Configuration
@ConditionalOnProperty(name = "groupbuy.mq.enabled", havingValue = "true")
public class GbMqConfig {

    public static final String EXCHANGE = "gb.notify.exchange";
    public static final String QUEUE_TEAM_COMPLETE = "gb.team.complete.queue";
    public static final String QUEUE_TEAM_FAIL = "gb.team.fail.queue";
    public static final String RK_TEAM_COMPLETE = "team.complete";
    public static final String RK_TEAM_FAIL = "team.fail";

    @Bean
    public TopicExchange gbNotifyExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue gbTeamCompleteQueue() {
        return QueueBuilder.durable(QUEUE_TEAM_COMPLETE).build();
    }

    @Bean
    public Queue gbTeamFailQueue() {
        return QueueBuilder.durable(QUEUE_TEAM_FAIL).build();
    }

    @Bean
    public Binding gbTeamCompleteBinding() {
        return BindingBuilder.bind(gbTeamCompleteQueue()).to(gbNotifyExchange()).with(RK_TEAM_COMPLETE);
    }

    @Bean
    public Binding gbTeamFailBinding() {
        return BindingBuilder.bind(gbTeamFailQueue()).to(gbNotifyExchange()).with(RK_TEAM_FAIL);
    }

    @Bean
    public Jackson2JsonMessageConverter gbJsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate gbRabbitTemplate(ConnectionFactory connectionFactory,
                                           Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
