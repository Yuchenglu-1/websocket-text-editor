package com.example.wsTextEditor.service;

import com.example.wsTextEditor.pojo.MessageDTO;
import com.example.wsTextEditor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationService.class);

    // 注入STOMP消息发送模板
    private final SimpMessagingTemplate messagingTemplate;
    
    // 注入用户仓库
    private final UserRepository userRepository;

    // 只存储在线用户的ID
    private final Set<String> onlineUsers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Autowired
    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate, UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    /**
     * 添加用户到在线列表
     * @param userId 用户ID
     */
    public void addUser(String userId) {
        onlineUsers.add(userId);
        logger.info("User added to online list: {}. Total online: {}", userId, getOnlineUserCount());
        // 广播在线用户更新
        broadcastOnlineUsersUpdate();
    }
    
    /**
     * 从在线列表中移除用户
     * @param userId 用户ID
     */
    public void removeUser(String userId) {
        onlineUsers.remove(userId);
        logger.info("User removed from online list: {}. Total online: {}", userId, getOnlineUserCount());
        // 广播在线用户更新
        broadcastOnlineUsersUpdate();
    }

    /**
     * 向特定用户发送消息 (私聊)
     * Spring会自动将消息路由到订阅了 "/user/{userId}/queue/messages" 的客户端
     * @param userId  目标用户的ID
     * @param message 消息内容
     */
    public void sendToUser(String userId, MessageDTO message) {

        messagingTemplate.convertAndSendToUser(userId, "/queue/messages", message);
        logger.info("Sending private message to user: {}", userId);
    }

    /**
     * 广播消息给所有订阅了公共主题的用户
     * @param message 消息内容
     */
    public void broadcastToAllUsers(MessageDTO message) {
        // 将消息发送到公共主题 "/topic/public"
        messagingTemplate.convertAndSend("/topic/public", message);
        logger.info("Broadcasting message to /topic/public");
    }

    /**
     * 广播在线用户更新
     */
    public void broadcastOnlineUsersUpdate() {
        try {
            // 创建一个简单的消息通知在线用户列表已更新
            MessageDTO message = new MessageDTO();
            message.setType("online-users");
            message.setData("Online users list updated");
            // 将消息发送到在线用户主题
            messagingTemplate.convertAndSend("/topic/online-users", message);
            logger.info("Broadcasting online users update to /topic/online-users");
        } catch (Exception e) {
            logger.error("Error broadcasting online users update: ", e);
        }
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }

    /**
     * 获取所有在线用户的列表
     */
    public Set<String> getOnlineUsers() {
        return new HashSet<>(onlineUsers);
    }
}