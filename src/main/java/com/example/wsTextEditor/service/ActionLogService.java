package com.example.wsTextEditor.service;

import com.example.wsTextEditor.model.ActionLog;
import com.example.wsTextEditor.repository.ActionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ActionLogService {
    @Autowired
    private  ActionLogRepository actionLogRepository;
    // 记录操作日志的内部方法
    public void logAction(String action, String description) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        ActionLog log = new ActionLog(username, action, description);
        actionLogRepository.save(log);
    }
    
    // 记录操作日志的内部方法（带文档ID）
    public void logAction(String action, String description, String documentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        ActionLog log = new ActionLog(username, action, description);
        log.setDocumentId(documentId);
        actionLogRepository.save(log);
    }

}
