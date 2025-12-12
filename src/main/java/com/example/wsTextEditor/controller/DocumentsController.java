package com.example.wsTextEditor.controller;

import com.example.wsTextEditor.model.Document;
import com.example.wsTextEditor.model.DocumentCollaborator;
import com.example.wsTextEditor.model.User;
import com.example.wsTextEditor.repository.DocumentCollaboratorRepository;
import com.example.wsTextEditor.repository.DocumentRepository;
import com.example.wsTextEditor.repository.UserRepository;
import com.example.wsTextEditor.service.DocumentPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文档控制器
 * 处理文档的保存、删除、更新标题和标签管理等操作
 */
@RestController
@RequestMapping("/documents")
public class DocumentsController {
    private static final Logger logger = LoggerFactory.getLogger(DocumentsController.class);
    
    /** 文档仓库，用于数据库操作 */
    @Autowired
    private DocumentRepository documentRepository;
    
    /** 用户仓库，用于用户相关数据库操作 */
    @Autowired
    private UserRepository userRepository;
    
    /** 文档权限服务，用于处理文档访问权限 */
    @Autowired
    private DocumentPermissionService documentPermissionService;
    
    /** 文档协作者仓库，用于协作者相关数据库操作 */
    @Autowired
    private DocumentCollaboratorRepository documentCollaboratorRepository;

    /**
     * 保存文档内容
     * @param documentId 文档唯一标识符
     * @param content 文档内容
     * @param language 编程语言类型
     * @param title 文档标题
     * @param userDetails 当前认证用户信息
     * @return 保存结果响应
     */
    @PostMapping("/save")
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<String> saveDocument(@RequestParam String documentId,
                                               @RequestParam String content,
                                               @RequestParam(required = false) String language,
                                               @RequestParam(required = false) String title,
                                               @AuthenticationPrincipal UserDetails userDetails) throws Exception{

        logger.info("Saving document with ID:{},user:{}", documentId, userDetails.getUsername());
        // 查找文档
        Document document = documentRepository.findByUniqueId(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid document ID:" + documentId));

        // 验证用户权限（只有文档所有者和编辑者才能保存）
        // 2. 查找用户
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (!documentPermissionService.canEditDocument(document, currentUser)) {
            logger.warn("User {} does not have permission to save document {}", userDetails.getUsername(), documentId);
            throw new IllegalAccessException("You don't have permission to save this document.");
        }
        // 更新文档内容
        document.setContent(content);
        if (language != null && !language.isEmpty()) {
            document.setLanguage(language);
        }
        if (title != null && !title.isEmpty()) {
            document.setTitle(title);
        }
        // 保存到数据库
        documentRepository.save(document);
        logger.info("Document {} saved successfully by user {}", documentId, userDetails.getUsername());
        return ResponseEntity.ok("Document saved successfully!");
    }
    /**
     * 删除文档
     * 只有文档所有者才有权限删除文档
     * @param docId 文档ID
     * @param userDetails 当前认证用户信息
     * @return 删除结果响应
     */
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW)
    @DeleteMapping("/delete/{docId}")
    public ResponseEntity<String> deleteDocument(@PathVariable String docId,
                                                 @AuthenticationPrincipal UserDetails userDetails)throws Exception {

            logger.info("Deleting document with ID: {}", docId);
            //获取当前用户，查看是否有删除权限
             User user=userRepository.findByUsername(userDetails.getUsername())
                     .orElseThrow(()->new IllegalStateException("no user exist"));
             //通过文档id查找文档
            Document document =documentRepository.findByUniqueId(docId)
                    .orElseThrow(()->new IllegalArgumentException("Invalid document ID;"));
             if(documentPermissionService.getUserPermissionLevel(document,user)== DocumentCollaborator.PermissionLevel.OWNER)
             {   
                 logger.info("User {} is owner, deleting document collaborators", userDetails.getUsername());
                 documentCollaboratorRepository.deleteByDocumentId(document.getId());
                 logger.info("Deleting document {}", docId);
                 documentRepository.delete(document);
             }
             else
             {
                 logger.warn("User {} does not have permission to delete document {}", userDetails.getUsername(), docId);
                throw new IllegalAccessException("You don't have permission to delete this document.");
             }
        logger.info("Document {} deleted successfully by user {}", docId, userDetails.getUsername());
        return ResponseEntity.ok("Document deleted successfully!");
    }
    @PostMapping("/update-title/{docId}")
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<String> updateDocumentTitle(@PathVariable String docId,
                                                     @RequestBody Map<String,String> tit,
                                                     @AuthenticationPrincipal UserDetails userDetails){

            logger.info("Updating title for document with ID: {}", docId);
            //获取当前用户，查看是否有更新权限
            User user=userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(()->new IllegalStateException("no user exist"));
            //通过文档id修改文档
            String title=tit.get("title");
            Document document =documentRepository.findByUniqueId(docId)
                    .orElseThrow(()->new IllegalArgumentException("Invalid document ID;"));
            document.setTitle(title);
            documentRepository.save(document);
            logger.info("Document {} title updated successfully by user {}", docId, userDetails.getUsername());
        return ResponseEntity.ok("Document title updated successfully!");
    }
    /**
     * 为文档添加标签
     * 只有具有编辑权限的用户才能为文档添加标签
     * @param docId 文档ID
     * @param payload 包含标签信息的请求体
     * @param userDetails 当前认证用户信息
     * @return 添加标签的结果响应
     */
    @PostMapping("/add-tags/{docId}")
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public ResponseEntity<String> addTagsToDocument(@PathVariable String docId,
                                                    @RequestBody Map<String, String> payload,
                                                    @AuthenticationPrincipal UserDetails userDetails)throws  Exception{

            logger.info("Adding tags to document with ID: {}", docId);
            // 获取当前用户
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException("User not found"));

            // 通过文档ID查找文档
            Document document = documentRepository.findByUniqueId(docId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid document ID: " + docId));

            // 检查用户是否有权限编辑文档
            if (!documentPermissionService.canEditDocument(document, user)) {
                logger.warn("User {} does not have permission to add tags to document {}", userDetails.getUsername(), docId);
                throw new IllegalAccessException("You don't have permission to add tags to this document.");
            }
            // 获取请求中的标签
            String tagsString = payload.get("tags");
            if (tagsString == null || tagsString.trim().isEmpty()) {
                logger.warn("Empty tags provided for document {}", docId);
                throw new IllegalArgumentException("Tags cannot be empty.");
            }
            // 解析标签（支持逗号分隔的多个标签）
            String[] newTags = tagsString.split(",");
            // 获取文档现有的标签
            List<String> existingTags = document.getTagList();
            // 合并现有标签和新标签，去重
            Set<String> tagSet = new HashSet<>(existingTags);
            for (String tag : newTags) {
                String trimmedTag = tag.trim();
                if (!trimmedTag.isEmpty()) {
                    tagSet.add(trimmedTag);
                }
            }
            // 更新文档标签
            document.setTagList(new ArrayList<>(tagSet));
            documentRepository.save(document);
            logger.info("Tags added successfully to document {} by user {}", docId, userDetails.getUsername());
            return ResponseEntity.ok("Tags added successfully!");
    }
    @GetMapping("/tags")
    public ResponseEntity<?> getTags(@AuthenticationPrincipal UserDetails userDetails)
    {   
        Set<String> tags = new HashSet<>();

            logger.info("Fetching tags for user: {}", userDetails.getUsername());
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            //读取自己有权限访问的文档列表的标签
            List<DocumentCollaborator> documentcollaborators = documentCollaboratorRepository.findByUser(user);
            List<Document> documents = documentcollaborators.stream()
                    .map(DocumentCollaborator::getDocument)
                    .toList();
            documents.forEach(document -> tags.addAll(document.getTagList()));
            logger.info("Found {} tags for user {}", tags.size(), userDetails.getUsername());
        return ResponseEntity.ok(tags);
    }
    /**
     * 根据标签获取文档列表
     * @param tag 标签名
     * @param userDetails 当前认证用户信息
     * @return 符合标签条件的文档列表
     */
    @GetMapping("/tags/{tag}")
    public ResponseEntity<?> getDocumentsByTag(@PathVariable String tag,@AuthenticationPrincipal UserDetails userDetails)
    {   
        List<Document> documents = new ArrayList<>();

            logger.info("Fetching documents by tag: {} for user: {}", tag, userDetails.getUsername());
            User user =userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(()->new IllegalStateException("User not found"));
           documents = documentRepository.findByTags(tag);
           logger.info("Found {} documents with tag {} for user {}", documents.size(), tag, userDetails.getUsername());
        return ResponseEntity.ok(documents);
    }
}