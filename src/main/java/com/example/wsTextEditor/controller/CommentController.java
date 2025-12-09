package com.example.wsTextEditor.controller;

import com.example.wsTextEditor.model.Comment;
import com.example.wsTextEditor.service.ActionLogService;
import com.example.wsTextEditor.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;
   @Autowired
   private ActionLogService actionLogService;
    // 获取所有评论
    @GetMapping
    public List<Comment> getAllComments() {
        return commentService.getAllComments();
    }

    // 根据文档ID获取评论
    @GetMapping("/document/{documentId}")
    public List<Comment> getCommentsByDocumentId(@PathVariable String documentId) {
        return commentService.getCommentsByDocumentId(documentId);
    }

    // 根据ID获取特定评论
    @GetMapping("/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable Long id) {
        return commentService.getCommentById(id)
                .map(comment -> ResponseEntity.ok().body(comment))
                .orElse(ResponseEntity.notFound().build());
    }

    // 创建新评论
    @PostMapping
    public Comment createComment(@RequestBody Comment comment,@AuthenticationPrincipal UserDetails userDetails) {
        String username=userDetails.getUsername();
        return commentService.createComment(comment,username);
    }

    // 删除评论
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id,@AuthenticationPrincipal UserDetails userDetails) {
        try { String username=userDetails.getUsername();
            Comment comment=commentService.getCommentById(id)
                    .orElseThrow(() -> new RuntimeException("Comment not found"));
            String documentId=comment.getDocumentId();
            String authorname=comment.getAuthor();
            actionLogService.logAction("删除评论","删除了"+"用户"+authorname+"的评论"+comment.getContent(),documentId);
            commentService.deleteComment(id,username);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    
    // 给评论点赞
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeComment(@PathVariable Long id) {
        try {
            // 获取当前认证的用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Comment comment = commentService.getCommentById(id)
                    .orElseThrow(() -> new RuntimeException("Comment not found"));
            String documentId=comment.getDocumentId();
            String authorname=comment.getAuthor();
            actionLogService.logAction("点赞评论","点赞了"+"用户"+authorname+"的评论"+"评论:"+comment.getContent(),documentId);
            Comment likedComment = commentService.likeComment(id, username);
            return ResponseEntity.ok(likedComment);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("未处理的异常: " + e.getMessage());
        }
    }
    
    // 取消评论点赞
    @PostMapping("/{id}/unlike")
    public ResponseEntity<Comment> unlikeComment(@PathVariable Long id) {
        try {
            // 获取当前认证的用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Comment unlikedComment = commentService.unlikeComment(id, username);
            return ResponseEntity.ok(unlikedComment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 获取回复评论
    @GetMapping("/replies/{parentCommentId}")
    public List<Comment> getRepliesByParentCommentId(@PathVariable Long parentCommentId) {
        return commentService.getRepliesByParentCommentId(parentCommentId);
    }
}