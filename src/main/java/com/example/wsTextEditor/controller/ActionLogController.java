package com.example.wsTextEditor.controller;

import com.example.wsTextEditor.model.ActionLog;
import com.example.wsTextEditor.repository.ActionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/logs")
public class ActionLogController {

    @Autowired
    private ActionLogRepository actionLogRepository;

    // 获取当前用户的所有操作日志
    @GetMapping
    public ResponseEntity<?> getUserLogs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        List<ActionLog> logs = actionLogRepository.findByUsernameOrderByCreatedAtDesc(username);
        return ResponseEntity.ok(logs);
    }
    // 根据操作类型获取日志
    @GetMapping("/action/{action}")
    public ResponseEntity<?> getLogsByAction(@PathVariable String action) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // 这里可以添加权限检查，确保用户只能查看自己的日志
        List<ActionLog> logs = actionLogRepository.findByActionOrderByCreatedAtDesc(action);
        
        // 过滤出当前用户的数据
        List<ActionLog> userLogs = logs.stream()
                .filter(log -> log.getUsername().equals(username))
                .toList();
                
        return ResponseEntity.ok(userLogs);
    }


}