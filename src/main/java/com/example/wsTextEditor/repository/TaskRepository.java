package com.example.wsTextEditor.repository;

import com.example.wsTextEditor.model.Task;
import com.example.wsTextEditor.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByDocumentId(String documentId);
}