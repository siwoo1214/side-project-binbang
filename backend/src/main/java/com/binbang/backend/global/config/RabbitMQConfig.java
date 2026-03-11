package com.binbang.backend.global.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;


@Configuration
public class RabbitMQConfig {

    // ======================== Queue 이름 설정 ======================
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";

    // Dead Letter Queue (DLQ) - 실패한 메시지 보관용
    public static final String EMAIL_DLQ = "email.dlq";
    public static final String NOTIFICATION_DLQ = "notification.dlq";

    // ==================== Exchange 이름 상수 ====================
    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    // DLQ Exchange
    public static final String DLQ_EXCHANGE = "dlq.exchange";

    // ==================== Routing Key 상수 ====================
    public static final String EMAIL_ROUTING_KEY = "email.routing.key";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.routing.key";

    // DLQ Routing Key
    public static final String EMAIL_DLQ_ROUTING_KEY = "email.dlq.routing.key";
    public static final String NOTIFICATION_DLQ_ROUTING_KEY = "notification.dlq.routing.key";

    // ==================== 1. Queue 정의 ====================

    /**
     * 이메일 발송 Queue
     * - Dead Letter Exchange 설정 (실패 시 DLQ로 이동)
     */
    @Bean
    public Queue emailQueue(){
        return QueueBuilder.durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange",DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key",EMAIL_DLQ_ROUTING_KEY)
                .build();
    }

    /**
     * 앱 내 알림 Queue
     * - Dead Letter Exchange 설정
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_DLQ_ROUTING_KEY)
                .build();
    }

    /**
     * 이메일 DLQ (Dead Letter Queue)
     * - 3번 재시도 후에도 실패한 메시지 보관
     */
    @Bean
    public Queue emailDLQ(){
        return QueueBuilder.durable(EMAIL_DLQ).build();
    }

    /**
     * 알림 DLQ
     */
    @Bean
    public Queue notificationDLQ(){
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }

    // ==================== 2. Exchange 정의 ====================

    /**
     * 이메일용 Direct Exchange
     */
    @Bean
    public DirectExchange emailExchange(){
        return new DirectExchange(EMAIL_EXCHANGE);
    }

    /**
     * 알림용 Direct Exchange
     */
    @Bean
    public DirectExchange notificationExchange(){
        return new DirectExchange(NOTIFICATION_EXCHANGE);
    }

    /**
     * DLQ용 Direct Exchange
     */
    @Bean
    public DirectExchange dlqExchange(){
        return new DirectExchange(DLQ_EXCHANGE);
    }

    // ==================== 3. Binding 정의 ====================

    /**
     * 이메일 Queue와 Exchange 바인딩
     */
    @Bean
    public Binding emailBinding(){
        return BindingBuilder
                .bind(emailQueue())
                .to(emailExchange())
                .with(EMAIL_ROUTING_KEY);
    }

    /**
     * 알림 Queue와 Exchange 바인딩
     */
    @Bean
    public Binding notificationBinding(){
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    /**
     * 이메일 DLQ 바인딩
     */
    @Bean
    public Binding emailDLQBinding(){
        return BindingBuilder
                .bind(emailDLQ())
                .to(dlqExchange())
                .with(EMAIL_DLQ_ROUTING_KEY);
    }

    /**
     * 알림 DLQ 바인딩
     */
    @Bean
    public Binding notificationDLQBinding() {
        return BindingBuilder
                .bind(notificationDLQ())
                .to(dlqExchange())
                .with(NOTIFICATION_DLQ_ROUTING_KEY);
    }

    // ==================== 4. Message Converter ====================

    @Bean
    @SuppressWarnings("deprecation")
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * RabbitTemplate 설정
     * - 메시지 발행(Produce)할 때 사용
     * - JSON 변환기 자동 적용
     */
    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    /**
     * Listener Container Factory 설정
     * - 메시지 소비(Consume)할 때 사용
     * - JSON 변환기 자동 적용
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }

}
