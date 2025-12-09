package com.example.wsTextEditor.model;

import jakarta.persistence.*;

import java.util.*;

/**
 * 文档实体类
 * 表示一个可协作编辑的文档
 */
@Entity
public class Document {

    /** 文档ID，主键，自动生成 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 文档唯一标识符 */
    @Column(unique = true, nullable = false)
    private String uniqueId;

    /** 文档所有者 */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    /** 文档标题 */
    private String title;
    
    /** 文档编程语言 */
    private String language;
    
    /** 文档内容，最大长度10000字符 */
    @Column(length = 10000)
    private String content;

    /** 文档标签，存储为逗号分隔的字符串 */
    @Column(name = "tags")
    private String tags; // 存储逗号分隔的标签


    /** 文档创建时间 */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;
    
    /** 文档更新时间 */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    /**
     * 实体持久化前回调方法
     * 设置文档唯一标识符和创建时间
     */
    @PrePersist
    public void prePersist() {
        if (uniqueId == null) {
            uniqueId = UUID.randomUUID().toString();
        }
        createdAt = new Date();
        updatedAt = new Date();
    }
    
    /**
     * 实体更新前回调方法
     * 更新文档修改时间
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = new Date();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }


    public User getOwner() {
        return owner;
    }


    public void setOwner(User owner) {
        this.owner = owner;
    }


    public String getTitle() {
        return title;
    }

    /**
     * 获取文档标签字符串
     * @return 逗号分隔的标签字符串
     */
    public String getTags() {
        return tags;
    }
    
    /**
     * 设置文档标签字符串
     * @param tags 逗号分隔的标签字符串
     */
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    /**
     * 获取文档标签列表
     * @return 标签列表
     */
    public List<String> getTagList() {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(tags.split(","));
    }
    
    /**
     * 设置文档标签列表
     * @param tagList 标签列表
     */
    public void setTagList(List<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            this.tags = null;
        } else {
            this.tags = String.join(",", tagList);
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }


    public String getContent() {
        return content;
    }


    public void setContent(String content) {
        this.content = content;
    }



    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }


    public Date getUpdatedAt() {
        return updatedAt;
    }


    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}