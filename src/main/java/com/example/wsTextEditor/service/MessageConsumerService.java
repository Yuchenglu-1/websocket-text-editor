package com.example.wsTextEditor.service;

import com.example.wsTextEditor.config.RabbitMQConfig;
import com.example.wsTextEditor.pojo.MessageDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MessageConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumerService.class);

    @Autowired
    private WebSocketNotificationService webSocketNotificationService;

    /**
     * 监听任务消息队列
     */
    @RabbitListener(queues = RabbitMQConfig.TASK_QUEUE)
    public void handleTaskMessage(MessageDTO message) {
        logger.info("收到任务消息: {}", message);
        // 在这里处理任务消息，例如：
        // 1. 推送消息到WebSocket客户端
        // 2. 发送邮件通知
        // 3. 更新缓存等
        processMessage(message);
    }

    /**
     * 监听评论消息队列
     */
    @RabbitListener(queues = RabbitMQConfig.COMMENT_QUEUE)
    public void handleCommentMessage(MessageDTO message) {
        logger.info("收到评论消息: {}", message);
        // 在这里处理评论消息
        processMessage(message);
    }

    /**
     * 处理消息的通用方法
     */
    private void processMessage(MessageDTO message) {
        // 根据消息类型和操作类型进行不同的处理
        switch (message.getType()) {
            case "task":
                handleTaskOperation(message);
                break;
            case "comment":
                handleCommentOperation(message);
                break;
            default:
                logger.warn("未知的消息类型: {}", message.getType());
        }
    }

    /**
     * 处理任务相关操作
     */
    private void handleTaskOperation(MessageDTO message) {
        switch (message.getAction()) {
            case "create":
                logger.info("处理新任务创建: {}", message.getData());
                // 实现任务创建后的处理逻辑
                broadcastToUsers(message);
                break;
            case "update":
                logger.info("处理任务更新: {}", message.getData());
                // 实现任务更新后的处理逻辑
                broadcastToUsers(message);
                break;
            case "delete":
                logger.info("处理任务删除: {}", message.getData());
                // 实现任务删除后的处理逻辑
                broadcastToUsers(message);
                break;
            default:
                logger.warn("未知的任务操作类型: {}", message.getAction());
        }
    }

    /**
     * 处理评论相关操作
     */
    private void handleCommentOperation(MessageDTO message) {
        switch (message.getAction()) {
            case "create":
                logger.info("处理新评论创建: {}", message.getData());
                // 例如推送到WebSocket连接的客户端
                broadcastToUsers(message);
                break;
            case "update":
                logger.info("处理评论更新: {}", message.getData());
                // 实现评论更新后的处理逻辑
                broadcastToUsers(message);
                break;
            case "like":
                logger.info("处理评论点赞: {}", message.getData());
                // 实现评论删除后的处理逻辑
                broadcastToUsers(message);
                break;
            default:
                logger.warn("未知的评论操作类型: {}", message.getAction());
        }
    }

    /**
     * 广播消息给在线用户
     */
    private void broadcastToUsers(MessageDTO message) {
        // 通过WebSocket推送消息给所有在线用户
        webSocketNotificationService.broadcastToAllUsers(message);
        logger.info("向在线用户广播消息: {}", message);
    }
}


