package com.example.wsTextEditor.controller;
import com.example.wsTextEditor.pojo.MessageDTO;
import com.example.wsTextEditor.service.WebSocketNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class MessageController {

    @Autowired
    private WebSocketNotificationService webSocketNotificationService;

    // 处理公共消息
    @MessageMapping("/sendPublicMessage")
    public MessageDTO sendPublicMessage(MessageDTO message) {
        // 可以在这里添加业务逻辑
        message.setTimestamp(LocalDateTime.now());
        // 广播消息
        webSocketNotificationService.broadcastToAllUsers(message);
        return message;
    }

    // 处理私聊消息
    @MessageMapping("/sendPrivateMessage")
    public void sendPrivateMessage(MessageDTO message) {
        message.setTimestamp(LocalDateTime.now());
        // 发送给指定用户
        webSocketNotificationService.sendToUser(message.getTargetUserId(), message);
    }
}