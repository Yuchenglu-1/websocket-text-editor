package com.example.wsTextEditor.repository;

import com.example.wsTextEditor.model.Document;
import com.example.wsTextEditor.model.DocumentCollaborator;
import com.example.wsTextEditor.model.User;
import com.example.wsTextEditor.model.DocumentCollaborator.PermissionLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 文档协作者数据访问接口
 * 用于管理文档与用户之间的协作关系
 * 继承JpaRepository以获得基本的CRUD操作功能
 */
public interface DocumentCollaboratorRepository extends JpaRepository<DocumentCollaborator, Long> {

    /**
     * 根据文档对象查找所有协作者记录
     * @param document 指定的文档对象
     * @return 该文档的所有协作者列表
     */
    List<DocumentCollaborator> findByDocument(Document document);

    /**
     * 根据用户对象查找该用户参与的所有文档协作记录
     * @param user 指定的用户对象
     * @return 该用户参与的所有文档协作列表
     */
    List<DocumentCollaborator> findByUser(User user);

    /**
     * 根据文档和用户查找特定的协作关系记录
     * @param document 指定的文档对象
     * @param user 指定的用户对象
     * @return 对应的协作者关系记录（可能为空）
     */
    Optional<DocumentCollaborator> findByDocumentAndUser(Document document, User user);

    /**
     * 根据文档和权限级别查找具有指定权限的所有协作者
     * @param document 指定的文档对象
     * @param permissionLevel 权限级别（如OWNER, EDITOR, VIEWER）
     * @return 具有指定权限级别的协作者列表
     */
    List<DocumentCollaborator> findByDocumentAndPermissionLevel(Document document, PermissionLevel permissionLevel);

    /**
     * 检查指定用户是否为指定文档的协作者
     * @param document 指定的文档对象
     * @param user 指定的用户对象
     * @return 如果用户是该文档的协作者则返回true，否则返回false
     */
    boolean existsByDocumentAndUser(Document document, User user);

    /**
     * 删除指定文档的所有协作者记录
     * 当文档被删除时，需要级联删除相关的协作关系记录
     * @param documentId 要删除协作者记录的文档对象
     */
    void deleteByDocumentId(long documentId);
     /**
     * 根据文档ID和用户ID查找特定的文档协作者关系记录
     * @param document 文档
     * @param userId 用户ID
     * @return 对应的文档协作者关系记录（可能为空）
     */
    Optional<DocumentCollaborator>findByDocumentAndUserId(Document document, long userId);
    /**
     * 根据文档ID和用户ID删除特定的文档协作者关系记录
     * @param document 文档
     * @param userId 用户ID
     */
    void deleteByDocumentAndUserId(Document document , long userId);
}
