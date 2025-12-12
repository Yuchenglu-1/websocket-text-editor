package com.example.wsTextEditor.controller;

import com.example.wsTextEditor.model.ActionLog;
import com.example.wsTextEditor.repository.ActionLogRepository;
import com.example.wsTextEditor.service.ActionLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ActionLogController.class);

    @Autowired
    private ActionLogRepository actionLogRepository;

    // 获取当前用户的所有操作日志
    @GetMapping
    public ResponseEntity<?> getUserLogs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        logger.info("Fetching action logs for user: {}", username);
        List<ActionLog> logs = actionLogRepository.findByUsernameOrderByCreatedAtDesc(username);
        logger.info("Found {} action logs for user: {}", logs.size(), username);
        return ResponseEntity.ok(logs);
    }
    // 根据操作类型获取日志
    @GetMapping("/action/{action}")
    public ResponseEntity<?> getLogsByAction(@PathVariable String action) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        logger.info("Fetching action logs for user: {} with action type: {}", username, action);
        // 这里可以添加权限检查，确保用户只能查看自己的日志
        List<ActionLog> logs = actionLogRepository.findByActionOrderByCreatedAtDesc(action);
        // 过滤出当前用户的数据
        List<ActionLog> userLogs = logs.stream()
                .filter(log -> log.getUsername().equals(username))
                .toList();
        logger.info("Found {} action logs for user: {} with action type: {}", userLogs.size(), username, action);
                
        return ResponseEntity.ok(userLogs);
    }
    @DeleteMapping("/clear")
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<?> clearLogs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
            logger.info("Clearing action logs for user: {}", username);
            // 这里添加权限检查，确保用户只能删除自己的日志
            List<ActionLog> logs = actionLogRepository.findByUsernameOrderByCreatedAtDesc(username);
            if (logs.isEmpty()) {
                logger.warn("No logs found for user: {}", username);
               throw new  IllegalArgumentException("No logs found for user: " + username);
            }
            // 删除当前用户所有的日志
            actionLogRepository.deleteAllByUsername(username);
            logger.info("Successfully cleared {} action logs for user: {}", logs.size(), username);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logs cleared successfully.");
            return ResponseEntity.ok(response);

    }


}