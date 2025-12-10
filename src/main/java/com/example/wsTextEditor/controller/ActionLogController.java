package com.example.wsTextEditor.controller;

import com.example.wsTextEditor.model.ActionLog;
import com.example.wsTextEditor.repository.ActionLogRepository;
import com.example.wsTextEditor.service.ActionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
    @DeleteMapping("/clear")
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<?> clearLogs() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            // 这里添加权限检查，确保用户只能删除自己的日志
            List<ActionLog> logs = actionLogRepository.findByUsernameOrderByCreatedAtDesc(username);
            if (logs.isEmpty()) {
                return ResponseEntity.status(404).body("No logs found for user: " + username);
            }
            // 删除当前用户所有的日志
            actionLogRepository.deleteAllByUsername(username);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logs cleared successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to clear logs: " + e.getMessage());
        }
    }


}