package com.example.wsTextEditor.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10000)
    private String content;

    private String author;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "document_id")
    private String documentId;
    
    // 点赞的用户
    @ElementCollection
    @CollectionTable(name = "comment_likes", joinColumns = @JoinColumn(name = "comment_id"))
    @Column(name = "username")
    private List<String> likedUsers;
    
    // 回复的评论ID（如果是回复评论的话）
    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    // Constructors
    public Comment() {
        this.createdAt = new Date();
        this.likedUsers = new ArrayList<>();
    }

    public Comment(String content, String author, String documentId) {
        this.content = content;
        this.author = author;
        this.documentId = documentId;
        this.createdAt = new Date();
        this.likedUsers = new ArrayList<>();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    
    public List<String> getLikedUsers() {
        return likedUsers;
    }
    
    public void setLikedUsers(List<String> likedUsers) {
        this.likedUsers = likedUsers;
    }
    
    // 获取点赞数
    public int getLikeCount() {
        return likedUsers != null ? likedUsers.size() : 0;
    }
    
    // 添加点赞用户
    public boolean addLikeUser(String username) {
        if (likedUsers == null) {
            likedUsers = new ArrayList<>();
        }
        // 检查用户是否已经点赞
        if (!likedUsers.contains(username)) {
            likedUsers.add(username);
            return true;
        }
        return false;
    }
    
    // 移除点赞用户
    public boolean removeLikeUser(String username) {
        if (likedUsers != null) {
            return likedUsers.remove(username);
        }
        return false;
    }
    
    public Long getParentCommentId() {
        return parentCommentId;
    }
    
    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }
}