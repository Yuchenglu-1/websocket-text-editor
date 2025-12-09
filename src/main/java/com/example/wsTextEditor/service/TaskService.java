package com.example.wsTextEditor.service;

import com.example.wsTextEditor.model.Document;
import com.example.wsTextEditor.model.DocumentCollaborator;
import com.example.wsTextEditor.model.Task;
import com.example.wsTextEditor.model.User;
import com.example.wsTextEditor.repository.DocumentRepository;
import com.example.wsTextEditor.repository.TaskRepository;
import com.example.wsTextEditor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 任务服务类
 * 提供任务的增删改查等业务逻辑处理
 */
@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private DocumentPermissionService documentPermissionService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageProducerService messageProducerService;
    
    /**
     * 获取所有任务
     * @return 任务列表
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * 根据文档ID获取任务列表
     * @param documentId 文档ID
     * @return 该文档下的所有任务
     */
    public List<Task> getTasksByDocumentUniqueId(String documentId) {
        return taskRepository.findByDocumentId(documentId);
    }

    /**
     * 根据ID获取特定任务
     * @param id 任务ID
     * @return 任务对象的Optional包装
     */
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    /**
     * 创建新任务
     * @param task 任务对象
     * @param username 创建者用户名
     * @return 保存后的任务对象
     */
    public Task createTask(Task task, String username) throws IllegalArgumentException, IllegalStateException {
        // 设置创建者
        task.setCreatedBy(username);
        
        // 验证权限
        if (!canEditTask(task, username)) {
            throw new IllegalStateException("You don't have permission to edit this task.");
        }
        
        // Task实体类会在构造函数中自动设置createdAt和updatedAt
        Task savedTask = taskRepository.save(task);
        
        // 发送任务创建通知
        sendTaskNotification(savedTask, "create");
        
        return savedTask;
    }


    /**
     * 删除任务
     * @param id 任务ID
     * @param username 用户名
     * @throws RuntimeException 当任务不存在时抛出异常
     */
    public void deleteTask(Long id, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        
        if (!canEditTask(task, username)) {
            throw new IllegalStateException("You don't have permission to edit this task.");
        }
        
        // 先发送删除通知，再删除任务
        sendTaskNotification(task, "delete");
        
        taskRepository.delete(task);
    }
    
    //判断是否有权限增删改任务
    public boolean canEditTask(Task task, String username) {
        Document document = documentRepository.findByUniqueId(task.getDocumentId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid document ID:" + task.getDocumentId()));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));
        return documentPermissionService.getUserPermissionLevel(document, user) == DocumentCollaborator.PermissionLevel.OWNER;
    }
    
    /**
     * 更新任务状态
     * @param id 任务ID
     * @param completed 完成状态
     * @param username 用户名
     * @return 更新后的任务对象
     */
    public Task updateTaskStatus(Long id, boolean completed, String username) throws IllegalArgumentException, IllegalStateException {
        // 查找任务
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + id));
        
        // 验证权限
        if (!canEditTask(task, username)) {
            throw new IllegalStateException("You don't have permission to edit this task.");
        }
        
        // 更新状态
        task.setCompleted(completed);
        Task updatedTask = taskRepository.save(task);
        
        // 发送任务更新通知
        sendTaskNotification(updatedTask, "update");
        
        return updatedTask;
    }
    
    /**
     * 发送任务通知
     * @param task 任务对象
     * @param action 操作类型 (create, update, delete)
     */
    private void sendTaskNotification(Task task, String action) {
        try {
            // 创建消息对象
            com.example.wsTextEditor.pojo.MessageDTO message = new com.example.wsTextEditor.pojo.MessageDTO();
            message.setType("task");
            message.setAction(action);
            message.setData(task);
            // 将Date转换为LocalDateTime
            message.setTimestamp(new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            
            // 发送到RabbitMQ，由MessageConsumerService处理并广播给WebSocket客户端
            messageProducerService.sendTaskMessage(message);
        } catch (Exception e) {
            System.err.println("Failed to send task notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}