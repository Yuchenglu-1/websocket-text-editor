package com.example.wsTextEditor.repository;

import com.example.wsTextEditor.model.Document;
import com.example.wsTextEditor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByUniqueId(String uniqueId);
    List<Document> findByOwner(User owner);
    // 根据标签查找文档
    List<Document> findByTags(String tag);
    // 根据用户和标签（模糊匹配）查找文档
    @Query("SELECT d FROM Document d WHERE LOWER(d.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<Document> findByTagContaining( @Param("tag") String tag);
}