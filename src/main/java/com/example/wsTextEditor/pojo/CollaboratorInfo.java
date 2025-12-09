package com.example.wsTextEditor.pojo;
import com.example.wsTextEditor.model.DocumentCollaborator;

public class CollaboratorInfo {
    private Long userId;
    private String username;
    private DocumentCollaborator.PermissionLevel permissionLevel;

    public CollaboratorInfo(Long userId, String username, DocumentCollaborator.PermissionLevel permissionLevel) {
        this.userId = userId;
        this.username = username;
        this.permissionLevel = permissionLevel;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public DocumentCollaborator.PermissionLevel getPermissionLevel() { return permissionLevel; }
    public void setPermissionLevel(DocumentCollaborator.PermissionLevel permissionLevel) { this.permissionLevel = permissionLevel; }
}