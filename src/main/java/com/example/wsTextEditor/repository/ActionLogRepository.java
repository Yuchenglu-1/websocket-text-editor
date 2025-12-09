package com.example.wsTextEditor.repository;

import com.example.wsTextEditor.model.ActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {
    List<ActionLog> findByUsernameOrderByCreatedAtDesc(String username);
    List<ActionLog> findByActionOrderByCreatedAtDesc(String action);
}