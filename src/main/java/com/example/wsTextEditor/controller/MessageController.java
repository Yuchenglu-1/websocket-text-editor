package com.example.wsTextEditor.controller;
import com.example.wsTextEditor.pojo.MessageDTO;
import com.example.wsTextEditor.service.WebSocketNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private WebSocketNotificationService webSocketNotificationService;

    // 处理公共消息
    @MessageMapping("/sendPublicMessage")
    public MessageDTO sendPublicMessage(MessageDTO message) {
        logger.info("Sending public message from user: {}", message.getSender());
        // 可以在这里添加业务逻辑
        message.setTimestamp(LocalDateTime.now());
        // 广播消息
        webSocketNotificationService.broadcastToAllUsers(message);
        logger.info("Public message broadcasted successfully");
        return message;
    }

    // 处理私聊消息
    @MessageMapping("/sendPrivateMessage")
    public void sendPrivateMessage(MessageDTO message) {
        logger.info("Sending private message from user {} to user {}", message.getSender(), message.getTargetUserId());
        message.setTimestamp(LocalDateTime.now());
        // 发送给指定用户
        webSocketNotificationService.sendToUser(message.getTargetUserId(), message);
        logger.info("Private message sent successfully to user {}", message.getTargetUserId());
    }
}