package com.example.wsTextEditor.controller;

import com.example.wsTextEditor.model.Comment;
import com.example.wsTextEditor.service.ActionLogService;
import com.example.wsTextEditor.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    private CommentService commentService;
   @Autowired
   private ActionLogService actionLogService;
    // 获取所有评论
    @GetMapping
    public List<Comment> getAllComments() {
        logger.info("Fetching all comments");
        return commentService.getAllComments();
    }

    // 根据文档ID获取评论
    @GetMapping("/document/{documentId}")
    public List<Comment> getCommentsByDocumentId(@PathVariable String documentId) {
        logger.info("Fetching comments for document ID: {}", documentId);
        return commentService.getCommentsByDocumentId(documentId);
    }

    // 根据ID获取特定评论
    @GetMapping("/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable Long id) {
        logger.info("Fetching comment by ID: {}", id);
        return commentService.getCommentById(id)
                .map(comment -> {
                    logger.info("Comment found with ID: {}", id);
                    return ResponseEntity.ok().body(comment);
                })
                .orElse(null );
    }

    // 创建新评论
    @PostMapping
    public Comment createComment(@RequestBody Comment comment,@AuthenticationPrincipal UserDetails userDetails) {
        String username=userDetails.getUsername();
        logger.info("Creating new comment by user: {}", username);
        return commentService.createComment(comment,username);
    }

    // 删除评论
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<?> deleteComment(@PathVariable Long id,@AuthenticationPrincipal UserDetails userDetails) {
        String username=userDetails.getUsername();
        logger.info("Deleting comment ID: {} by user: {}", id, username);
        Comment comment=commentService.getCommentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        String documentId=comment.getDocumentId();
        String authorname=comment.getAuthor();
        actionLogService.logAction("删除评论","删除了"+"用户"+authorname+"的评论"+comment.getContent(),documentId);
        commentService.deleteComment(id,username);
        logger.info("Comment ID: {} deleted successfully by user: {}", id, username);
        return ResponseEntity.ok().build();
    }
    
    // 给评论点赞
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeComment(@PathVariable Long id) {
        // 获取当前认证的用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        logger.info("User {} liking comment ID: {}", username, id);
        Comment comment = commentService.getCommentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        String documentId=comment.getDocumentId();
        String authorname=comment.getAuthor();
        actionLogService.logAction("点赞评论","点赞了"+"用户"+authorname+"的评论"+"评论:"+comment.getContent(),documentId);
        Comment likedComment = commentService.likeComment(id, username);
        logger.info("Comment ID: {} liked successfully by user: {}", id, username);
        return ResponseEntity.ok(likedComment);
    }
    
    // 取消评论点赞
    @PostMapping("/{id}/unlike")
    public ResponseEntity<Comment> unlikeComment(@PathVariable Long id) {
        // 获取当前认证的用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        logger.info("User {} unliking comment ID: {}", username, id);
        
        Comment unlikedComment = commentService.unlikeComment(id, username);
        logger.info("Comment ID: {} unliked successfully by user: {}", id, username);
        return ResponseEntity.ok(unlikedComment);
    }
    
    // 获取回复评论
    @GetMapping("/replies/{parentCommentId}")
    public List<Comment> getRepliesByParentCommentId(@PathVariable Long parentCommentId) {
        logger.info("Fetching replies for parent comment ID: {}", parentCommentId);
        return commentService.getRepliesByParentCommentId(parentCommentId);
    }
}