package com.example.wsTextEditor.controller;

import com.example.wsTextEditor.model.ActionLog;
import com.example.wsTextEditor.model.Document;
import com.example.wsTextEditor.model.DocumentCollaborator;
import com.example.wsTextEditor.model.User;
import com.example.wsTextEditor.pojo.CollaboratorInfo;
import com.example.wsTextEditor.repository.DocumentCollaboratorRepository;
import com.example.wsTextEditor.repository.DocumentRepository;
import com.example.wsTextEditor.repository.UserRepository;
import com.example.wsTextEditor.service.ActionLogService;
import com.example.wsTextEditor.service.DocumentPermissionService;
import com.sun.xml.bind.v2.TODO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentCollaboratorController {
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private DocumentPermissionService documentPermissionService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DocumentCollaboratorRepository documentCollaboratorRepository;
    @Autowired
    private ActionLogService actionLogService;
    // 邀请用户协作API端点（通过邀请UUID）
    @PostMapping("/{documentId}/invite-by-uuid")
    @ResponseBody
    public ResponseEntity<String> inviteUserByUuid(@PathVariable String documentId,
                                                   @RequestParam String invitationUuid,
                                                   @RequestParam DocumentCollaborator.PermissionLevel permissionLevel,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        try {

            Document document = documentRepository.findByUniqueId(documentId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid document ID:" + documentId));

            User invitingUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

            boolean success = documentPermissionService.inviteUserToDocumentByUuid(document, invitingUser, invitationUuid, permissionLevel);

            if (success) {
                actionLogService.logAction("邀请用户参与文档协作","邀请用户"+invitingUser+"参与文档协作"+"文档的ID:"+documentId);
                return ResponseEntity.ok("User invited successfully!");
            } else {
                return ResponseEntity.badRequest().body("Failed to invite user. Check if user exists and is not already a collaborator.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to invite user: " + e.getMessage());
        }
    }
    // 获取文档协作者列表API端点
    @GetMapping("/{documentId}/collaborators")
    @ResponseBody
    public ResponseEntity<List<CollaboratorInfo>> getDocumentCollaborators(@PathVariable String documentId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Document document = documentRepository.findByUniqueId(documentId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid document ID:" + documentId));

            User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

            // 检查用户是否有权限查看协作者列表
            if (!documentPermissionService.canViewDocument(document, currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<DocumentCollaborator> collaborators = documentPermissionService.getDocumentCollaborators(document);
            List<CollaboratorInfo> collaboratorInfos = collaborators.stream()
                    .map(collaborator -> new CollaboratorInfo(
                            collaborator.getUser().getId(),
                            collaborator.getUser().getUsername(),
                            collaborator.getPermissionLevel()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(collaboratorInfos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    // 更新文档协作者权限API端点
    @PutMapping("/{docId}/collaborators/{userId}/permission")
    public ResponseEntity<String> updateCollaboratorPermission(@PathVariable String docId,
                                                              @PathVariable String userId,
                                                              @RequestBody Map<String,String> permissionLevelmap
                                                              )
    {

        try{
            Document document = documentRepository.findByUniqueId(docId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid document ID:" + docId));
            //获取协作者
           DocumentCollaborator collaborator = documentCollaboratorRepository.findByDocumentAndUserId(document,Long.parseLong(userId))
                   .orElseThrow(()->new IllegalArgumentException("Invalid Argument"+userId+"or"+docId));
           //将字符串转为枚举类
            String permissionStr = permissionLevelmap.get("permissionLevel");
            DocumentCollaborator.PermissionLevel permissionLevel = DocumentCollaborator.PermissionLevel.valueOf(permissionStr);
            collaborator.setPermissionLevel(permissionLevel);
           documentCollaboratorRepository.save(collaborator);
        }catch(IllegalArgumentException e)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Argument");
        }
        catch(Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update collaborator permission: " + e.getMessage());
        }
        return ResponseEntity.ok("Collaborator permission updated successfully!");

    }
    //删除文档协作者API端点
    @DeleteMapping("/{docId}/collaborators/{userId}")
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public ResponseEntity<String> removeCollaborator(@PathVariable String docId,
                                                    @PathVariable String userId,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(()->new IllegalArgumentException("Invalid user"));
            Document document = documentRepository.findByUniqueId(docId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid document ID:" + docId));
           documentCollaboratorRepository.deleteByDocumentAndUserId(document,Long.parseLong(userId));
           actionLogService.logAction("删除文档协作者","删除文档协作者"+"文档的ID:"+docId+"用户ID:"+userId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove collaborator: " + e.getMessage());
        }
        return ResponseEntity.ok("Collaborator removed successfully!");
    }
}
