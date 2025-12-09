package com.example.wsTextEditor.pojo;

import java.time.LocalDateTime;

public class MessageDTO {
    private String type; // 消息类型: task, comment
    private String action; // 操作类型: create, update, delete
    private Object data; // 消息数据
    private LocalDateTime timestamp; // 时间戳
    private String sender; // 发送者
    private String targetUserId; // 目标用户ID，用于私聊

    // 构造函数
    public MessageDTO() {
        this.timestamp = LocalDateTime.now();
    }
    
    public MessageDTO(String type, String action, Object data, String sender) {
        this.type = type;
        this.action = action;
        this.data = data;
        this.sender = sender;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSender() {
        return sender;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }
    
    @Override
    public String toString() {
        return "MessageDTO{" +
                "type='" + type + '\'' +
                ", action='" + action + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                ", sender='" + sender + '\'' +
                '}';
    }
}