package com.example.wsTextEditor.repository;

import com.example.wsTextEditor.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDocumentId(String documentId);
    List<Comment> findByParentCommentId(Long parentCommentId);
}