package com.example.wsTextEditor.model;

import jakarta.persistence.*;

@Entity
@Table(name = "document_collaborators")
public class DocumentCollaborator {
    
    public enum PermissionLevel {
        OWNER,      // 文档所有者（管理员）
        EDITOR,     // 编辑者
        VIEWER      // 查看者
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionLevel permissionLevel;
    
    // Constructors
    public DocumentCollaborator() {}
    
    public DocumentCollaborator(Document document, User user, PermissionLevel permissionLevel) {
        this.document = document;
        this.user = user;
        this.permissionLevel = permissionLevel;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Document getDocument() {
        return document;
    }
    
    public void setDocument(Document document) {
        this.document = document;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }
    
    public void setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
    }
}