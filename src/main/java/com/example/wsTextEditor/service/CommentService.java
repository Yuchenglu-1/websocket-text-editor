package com.example.wsTextEditor.service;

import com.example.wsTextEditor.model.*;
import com.example.wsTextEditor.repository.CommentRepository;
import com.example.wsTextEditor.repository.DocumentRepository;
import com.example.wsTextEditor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private DocumentPermissionService documentPermissionService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private MessageProducerService messageProducerService;
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public List<Comment> getCommentsByDocumentId(String documentId) {
        return commentRepository.findByDocumentId(documentId);
    }

    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    public Comment createComment(Comment comment,String username) {
        comment.setCreatedAt(new Date());
        // 确保likedUsers列表已初始化
        if (comment.getLikedUsers() == null) {
            comment.setLikedUsers(new ArrayList<>());
        }

        comment.setAuthor(username);
        sendCommentNotification(comment, "create");
        return commentRepository.save(comment);
    }


    //删除评论功能

    public void deleteComment(Long id,String username)throws RuntimeException {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        User user=userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        Document document=documentRepository.findByUniqueId(comment.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + comment.getDocumentId()));
        // 检查用户是否是评论作者或文档所有者否则不能删除
        if(!comment.getAuthor().equals(username)&&documentPermissionService.getUserPermissionLevel(document,user)!= DocumentCollaborator.PermissionLevel.OWNER){
            throw new RuntimeException("You are not authorized to delete this comment.");
        }
        commentRepository.delete(comment);
    }
    
    /**
     * 给评论点赞
     * @param id 评论ID
     * @param username 点赞用户的用户名
     * @return 更新后的评论
     */
    public Comment likeComment(Long id, String username) throws IllegalStateException {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        
        // 添加用户到点赞列表
        if (!comment.addLikeUser(username)) {
            throw new IllegalStateException("User has already liked this comment.");
        }
        //点赞的消息只有传递是谁点赞的
        com.example.wsTextEditor.pojo.MessageDTO message = new com.example.wsTextEditor.pojo.MessageDTO();
        message.setType("comment");
        message.setAction("like");
        message.setData(comment.getAuthor());
        message.setSender(username);
        // 发送到RabbitMQ，由MessageConsumerService处理并广播给WebSocket客户端
        messageProducerService.sendTaskMessage(message);
        return commentRepository.save(comment);
    }
    
    /**
     * 取消评论点赞
     * @param id 评论ID
     * @param username 取消点赞用户的用户名
     * @return 更新后的评论
     */
    public Comment unlikeComment(Long id, String username) throws IllegalStateException {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        
        // 从点赞列表中移除用户
        if (!comment.removeLikeUser(username)) {
            throw new IllegalStateException("User has not liked this comment.");
        }
        return commentRepository.save(comment);
    }
    
    /**
     * 获取回复评论
     * @param parentCommentId 父评论ID
     * @return 回复列表
     */
    public List<Comment> getRepliesByParentCommentId(Long parentCommentId) {
        // 注意：这需要在CommentRepository中添加相应的方法
        return commentRepository.findByParentCommentId(parentCommentId);
    }
    //发送消息，只有创建评论有消息还有某人点赞了A的评论，那么A会收到消息有人点赞了你的评论
    private void sendCommentNotification(Comment comment, String action) {

            // 创建消息对象
            com.example.wsTextEditor.pojo.MessageDTO message = new com.example.wsTextEditor.pojo.MessageDTO();
            message.setType("comment");
            message.setAction(action);
            message.setData(comment);
            // 将Date转换为LocalDateTime
            message.setTimestamp(new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

            // 发送到RabbitMQ，由MessageConsumerService处理并广播给WebSocket客户端
            messageProducerService.sendTaskMessage(message);
    }
}