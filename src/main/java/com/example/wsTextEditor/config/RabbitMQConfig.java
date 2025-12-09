package com.example.wsTextEditor.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // --- 常量定义 (保持不变) ---
    public static final String TASK_EXCHANGE = "task.exchange";
    public static final String COMMENT_EXCHANGE = "comment.exchange";
    public static final String TASK_QUEUE = "task.queue";
    public static final String COMMENT_QUEUE = "comment.queue";
    public static final String TASK_ROUTING_KEY = "task.notify";
    public static final String COMMENT_ROUTING_KEY = "comment.notify";

    // --- 队列和交换机定义 (保持不变) ---
    @Bean
    public TopicExchange taskExchange() {
        return new TopicExchange(TASK_EXCHANGE);
    }

    @Bean
    public TopicExchange commentExchange() {
        return new TopicExchange(COMMENT_EXCHANGE);
    }

    @Bean
    public Queue taskQueue() {
        return new Queue(TASK_QUEUE, true);
    }

    @Bean
    public Queue commentQueue() {
        return new Queue(COMMENT_QUEUE, true);
    }

    @Bean
    public Binding taskBinding() {
        return BindingBuilder.bind(taskQueue()).to(taskExchange()).with(TASK_ROUTING_KEY);
    }

    @Bean
    public Binding commentBinding() {
        return BindingBuilder.bind(commentQueue()).to(commentExchange()).with(COMMENT_ROUTING_KEY);
    }

    // --- 新增的核心配置：JSON转换器 ---

    /**
     * 定义一个全局的消息转换器Bean，用于将对象序列化为JSON格式。
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 自定义RabbitTemplate Bean，并为其设置JSON消息转换器。
     */
    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}