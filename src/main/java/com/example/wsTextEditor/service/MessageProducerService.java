package com.example.wsTextEditor.service;

import com.example.wsTextEditor.config.RabbitMQConfig;
import com.example.wsTextEditor.pojo.MessageDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageProducerService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送任务相关消息
     */
    public void sendTaskMessage(MessageDTO message) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.TASK_EXCHANGE,
            RabbitMQConfig.TASK_ROUTING_KEY,
            message
        );
    }

    /**
     * 发送评论相关消息
     */
    public void sendCommentMessage(MessageDTO message) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.COMMENT_EXCHANGE,
            RabbitMQConfig.COMMENT_ROUTING_KEY,
            message
        );
    }

    /**
     * 广播消息给所有用户
     */
    public void broadcastMessage(MessageDTO message) {
        // 同时发送到任务和评论两个交换机
        sendTaskMessage(message);
        sendCommentMessage(message);
    }
}