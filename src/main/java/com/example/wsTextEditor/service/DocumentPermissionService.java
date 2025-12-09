package com.example.wsTextEditor.service;

import com.example.wsTextEditor.model.Document;
import com.example.wsTextEditor.model.DocumentCollaborator;
import com.example.wsTextEditor.model.User;
import com.example.wsTextEditor.model.DocumentCollaborator.PermissionLevel;
import com.example.wsTextEditor.repository.DocumentCollaboratorRepository;
import com.example.wsTextEditor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 文档权限服务类
 * 处理文档的权限控制，包括协作者管理、权限验证等
 */
@Service
public class DocumentPermissionService {
    
    /** 文档协作者仓库 */
    @Autowired
    private DocumentCollaboratorRepository documentCollaboratorRepository;
    
    /** 用户仓库 */
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 初始化文档权限（文档创建时调用）
     * @param document 文档
     * @param owner 文档所有者
     */
    public void initializeDocumentPermissions(Document document, User owner) {
        DocumentCollaborator ownerCollaborator = new DocumentCollaborator(document, owner, PermissionLevel.OWNER);
        documentCollaboratorRepository.save(ownerCollaborator);
    }
    
    /**
     * 邀请用户协作编辑文档（通过用户名）
     * @param document 文档
     * @param invitingUser 邀请人（必须是文档所有者或管理员）
     * @param username 被邀请用户名
     * @param permissionLevel 权限级别
     * @return 是否邀请成功
     */
    public boolean inviteUserToDocument(Document document, User invitingUser, String username, PermissionLevel permissionLevel) {
        // 检查邀请人是否有权限邀请他人
        if (!canInviteUsers(document, invitingUser)) {
            return false;
        }
        
        // 查找被邀请用户
        Optional<User> invitedUserOpt = userRepository.findByUsername(username);
        if (invitedUserOpt.isEmpty()) {
            return false;
        }
        
        User invitedUser = invitedUserOpt.get();
        
        // 检查用户是否已经是协作者
        if (documentCollaboratorRepository.existsByDocumentAndUser(document, invitedUser)) {
            return false;
        }
        
        // 创建协作者记录
        DocumentCollaborator collaborator = new DocumentCollaborator(document, invitedUser, permissionLevel);
        documentCollaboratorRepository.save(collaborator);
        
        return true;
    }
    
    /**
     * 邀请用户协作编辑文档（通过邀请UUID）
     * @param document 文档
     * @param invitingUser 邀请人（必须是文档所有者或管理员）
     * @param invitationUuid 被邀请用户的邀请UUID
     * @param permissionLevel 权限级别
     * @return 是否邀请成功
     */
    public boolean inviteUserToDocumentByUuid(Document document, User invitingUser, String invitationUuid, PermissionLevel permissionLevel) {
        // 检查邀请人是否有权限邀请他人
        if (!canInviteUsers(document, invitingUser)) {
            return false;
        }
        
        // 查找被邀请用户
        Optional<User> invitedUserOpt = userRepository.findByInvitationUuid(invitationUuid);
        if (invitedUserOpt.isEmpty()) {
            return false;
        }
        
        User invitedUser = invitedUserOpt.get();
        
        // 检查用户是否已经是协作者
        if (documentCollaboratorRepository.existsByDocumentAndUser(document, invitedUser)) {
            return false;
        }
        
        // 创建协作者记录
        DocumentCollaborator collaborator = new DocumentCollaborator(document, invitedUser, permissionLevel);
        documentCollaboratorRepository.save(collaborator);
        
        return true;
    }
    
    /**
     * 检查用户是否可以邀请他人
     * @param document 文档
     * @param user 用户
     * @return 是否有权限
     */
    public boolean canInviteUsers(Document document, User user) {
        Optional<DocumentCollaborator> collaboratorOpt = documentCollaboratorRepository.findByDocumentAndUser(document, user);
        if (collaboratorOpt.isEmpty()) {
            return false;
        }
        
        PermissionLevel level = collaboratorOpt.get().getPermissionLevel();
        return level == PermissionLevel.OWNER;
    }
    
    /**
     * 获取用户的文档权限级别
     * @param document 文档
     * @param user 用户
     * @return 权限级别
     */
    public PermissionLevel getUserPermissionLevel(Document document, User user) {
        // 文档所有者始终具有OWNER权限
        if (document.getOwner().getId().equals(user.getId())) {
            return PermissionLevel.OWNER;
        }
        
        Optional<DocumentCollaborator> collaboratorOpt = documentCollaboratorRepository.findByDocumentAndUser(document, user);
        if (collaboratorOpt.isPresent()) {
            return collaboratorOpt.get().getPermissionLevel();
        }
        
        // 默认为VIEWER权限
        return PermissionLevel.VIEWER;
    }
    
    /**
     * 检查用户是否可以编辑文档
     * @param document 文档
     * @param user 用户
     * @return 是否可以编辑
     */
    public boolean canEditDocument(Document document, User user) {
        PermissionLevel level = getUserPermissionLevel(document, user);
        return level == PermissionLevel.OWNER || level == PermissionLevel.EDITOR;
    }
    
    /**
     * 检查用户是否可以查看文档
     * @param document 文档
     * @param user 用户
     * @return 是否可以查看
     */
    public boolean canViewDocument(Document document, User user) {
        // 所有协作者都可以查看文档
        return getUserPermissionLevel(document, user) != null;
    }
    
    /**
     * 获取文档的所有协作者
     * @param document 文档
     * @return 协作者列表
     */
    public List<DocumentCollaborator> getDocumentCollaborators(Document document) {
        return documentCollaboratorRepository.findByDocument(document);
    }
    

}