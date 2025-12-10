package com.example.wsTextEditor.controller;

import com.example.wsTextEditor.model.Task;
import com.example.wsTextEditor.service.ActionLogService;
import com.example.wsTextEditor.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 任务控制器
 * 处理任务相关的REST API请求
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private ActionLogService actionLogService;

    /**
     * 获取所有任务
     * GET /api/tasks
     * @return 任务列表
     */
    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    /**
     * 根据文档ID获取任务
     * GET /api/tasks/document/{documentId}
     * @param documentId 文档UUID
     * @return 该文档下的所有任务
     */
    @GetMapping("/document/{documentId}")
    public List<Task> getTasksByDocumentId(@PathVariable String documentId) {
        return taskService.getTasksByDocumentUniqueId(documentId);
    }

    /**
     * 根据ID获取特定任务
     * GET /api/tasks/{id}
     * @param id 任务ID
     * @return 任务对象
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(task -> ResponseEntity.ok().body(task))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建新任务
     * POST /api/tasks
     * @param task 任务对象
     * @return 创建后的任务对象
     * 只有文档的拥有者才可以进行任务的创建
     */
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody Task task, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userDetails.getUsername();
            Task createdTask = taskService.createTask(task, username);
            return ResponseEntity.ok(createdTask);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch(IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    /**
     * 更新任务状态
     * PUT /api/tasks/{id}/status
     * @param id 任务ID
     * @param taskDetails 包含完成状态的任务对象
     * @return 更新后的任务对象
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long id, @RequestBody Task taskDetails, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userDetails.getUsername();
            Task updatedTask = taskService.updateTaskStatus(id, taskDetails.isCompleted(), username);
            return ResponseEntity.ok(updatedTask);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch(IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 删除任务
     * DELETE /api/tasks/{id}
     * @param id 任务ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<?> deleteTask(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userDetails.getUsername();
            Task task=taskService.getTaskById(id)
                    .orElseThrow(() -> new IllegalArgumentException("任务不存在"));
            String documentId=task.getDocumentId();
            actionLogService.logAction( "删除任务", "任务ID："+id+"，文档ID："+documentId,documentId);
            taskService.deleteTask(id, username);
            return ResponseEntity.ok().build();
        } catch(IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch(IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}