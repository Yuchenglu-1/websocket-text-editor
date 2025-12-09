package com.example.wsTextEditor.model;

import jakarta.persistence.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;


@Document(indexName="documents")
public class DocumentSearchEntity {
    //定义基本属性
    @Id
    private Long id;
    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String content;
    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String title;
    @Field(type = FieldType.Keyword)
    private String language;
    @Field(type = FieldType.Keyword)
    private String uniqueId;
    @Field(type = FieldType.Date)
    private Date createAt;
    @Field(type = FieldType.Date)
    private Date updateAt;
    //定义set跟get函数
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
    public String getTitle() {
        return title;
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
    public String getUniqueId() {
        return uniqueId;
    }
    public void setUniqueId(String uuid) {
        this.uniqueId = uuid;
    }
    public Date getCreateAt() {
        return createAt;
    }
    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }
    public Date getUpdateAt() {
        return updateAt;
    }
    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }
    //构造函数初始化更新时间
    public DocumentSearchEntity(){
    }



}
