package com.example.wsTextEditor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "action_logs")
public class ActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //文档ID
    private String documentId;

    private String username;
    private String action;
    private String description;
    private String costTime;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors
    public ActionLog() {
        this.createdAt = LocalDateTime.now();
    }

    public ActionLog(String username, String action, String description) {
        this();
        this.username = username;
        this.action = action;
        this.description = description;

    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public void setCostTime(String costTime) {
        this.costTime = costTime;
    }
    public String getCostTime() {
        return costTime;
    }
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    public String getDocumentId() {
        return documentId;
    }
}