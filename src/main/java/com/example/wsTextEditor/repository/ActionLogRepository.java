package com.example.wsTextEditor.repository;

import com.example.wsTextEditor.model.ActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {
    List<ActionLog> findByUsernameOrderByCreatedAtDesc(String username);
    List<ActionLog> findByActionOrderByCreatedAtDesc(String action);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ActionLog a WHERE a.username = :username")
    void deleteAllByUsername(@Param("username") String username);
}