package com.example.wsTextEditor.aop;


import com.example.wsTextEditor.model.ActionLog;
import com.example.wsTextEditor.model.Comment;
import com.example.wsTextEditor.model.Task;
import com.example.wsTextEditor.service.ActionLogService;
import com.example.wsTextEditor.service.MessageConsumerService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;


@Aspect
@Component
public class ActionLogAspect {
    // 日志记录
    private static final Logger logger = LoggerFactory.getLogger(ActionLogAspect.class);
    @Autowired
    ActionLogService actionService;

    // 面向CUD操作日志
    @Around("execution(* com.example.wsTextEditor.controller.*.delete*(..))||" +
            "execution(* com.example.wsTextEditor.controller.*.create*(..))||" +
            "execution(* com.example.wsTextEditor.controller.*.update*(..))" +
            "||execution(* com.example.wsTextEditor.controller.*.save*(..))||" +
            "execution(* com.example.wsTextEditor.controller.*.remove*(..))")
    public Object OperateCUDLog(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("开始执行操作");
        Object result = joinPoint.proceed();
        ActionLog aclog = new ActionLog();
        //分CUD操作
        String actionname = joinPoint.getSignature().getName();
        if (actionname.startsWith("create")) {
            aclog.setAction("创建");
            String className = joinPoint.getTarget().getClass().getName();
            if (className.startsWith("com.example.wsTextEditor.controller.Editor")) {
                aclog.setDescription("创建文档");
            }
            //记录评论和任务操作
            else {
                if (joinPoint.getArgs().length > 0) {
                    String type = actionname.substring(6);
                    aclog.setDescription("创建" + type + joinPoint.getArgs()[0]);
                    Object[] args = joinPoint.getArgs();
                    if(args[0].getClass().getName().equals("com.example.wsTextEditor.model.Comment"))
                    {   Comment comment = (Comment) args[0];
                        aclog.setDescription("创建评论：" +comment.getContent() );
                        aclog.setDocumentId(comment.getDocumentId());
                    }
                    else
                    {   Task task = (Task) args[0];
                        aclog.setDescription("创建任务：" + task.getTitle());
                        aclog.setDocumentId(task.getDocumentId());
                    }
                }
            }
        } else if (actionname.startsWith("update")) {
            aclog.setAction("更新");
            String className = joinPoint.getTarget().getClass().getName();
            if (className.startsWith("com.example.wsTextEditor.controller.Document")) {
                if (joinPoint.getArgs().length > 0) {
                    String type = actionname.substring(6);
                    aclog.setDescription("更新并保存文档：" + type + "   文档id：" + joinPoint.getArgs()[0]);
                    Object[] args = joinPoint.getArgs();
                    String documentId = (String)args[0];
                    aclog.setDocumentId(documentId);
                }
            }
            //任务操作
            else {
                if (joinPoint.getArgs().length > 0) {
                    String type = actionname.substring(6);
                    Object[] args = joinPoint.getArgs();
                    Task task = (Task) args[1];
                    aclog.setDescription("更新任务：" + task.getTitle());
                    aclog.setDocumentId(task.getDocumentId());
                }
            }
        } else if (actionname.startsWith("delete") || actionname.startsWith("remove")) {
            aclog.setAction("删除");
            String className = joinPoint.getTarget().getClass().getName();
            if (className.startsWith("com.example.wsTextEditor.controller.Documents")) {
                if (joinPoint.getArgs().length > 0) {
                    String type = actionname.substring(6);
                    aclog.setDescription("删除文档：" + type + joinPoint.getArgs()[0]);
                    Object[] args = joinPoint.getArgs();
                    String documentId = (String)args[0];
                    aclog.setDocumentId(documentId);
                }
            }//评论和任务操作放在Controller里面写
            else {
               return result;
            }
        } else {
            aclog.setAction("保存");
            aclog.setDescription("保存文档" + joinPoint.getArgs()[0]);
            Object[] args = joinPoint.getArgs();
            String documentId = (String)args[0];
            aclog.setDocumentId(documentId);
        }
        logger.info("结束执行操作,记录日志{}", aclog);
        // 使用带documentId的logAction方法
        if (aclog.getDocumentId() != null) {
            actionService.logAction(aclog.getAction(), aclog.getDescription(), aclog.getDocumentId());
        } else {
            actionService.logAction(aclog.getAction(), aclog.getDescription());
        }
        return result;
    }
}

