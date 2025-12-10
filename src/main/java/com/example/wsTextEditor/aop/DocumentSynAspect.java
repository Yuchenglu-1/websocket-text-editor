package com.example.wsTextEditor.aop;

import com.example.wsTextEditor.model.Document;
import com.example.wsTextEditor.repository.DocumentRepository;
import com.example.wsTextEditor.service.DocumentSyncService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DocumentSynAspect {
    // 日志记录
    private static final Logger logger = LoggerFactory.getLogger(ActionLogAspect.class);
    private final DocumentSyncService documentSynService;
    private final DocumentRepository documentRepository;
    @Autowired
    DocumentSynAspect(DocumentSyncService documentSynService, DocumentRepository documentRepository)
    {
        this.documentSynService = documentSynService;
        this.documentRepository = documentRepository;
    }
    //删除文档时同步
    @Around("execution(* com.example.wsTextEditor.controller.DocumentsController.delete*(..))")
    public Object deleteSyncDocument(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("开始执行删除同步操作");
        documentSynService.deleteDocumentFromIndex(joinPoint.getArgs()[0].toString());
        Object result = joinPoint.proceed();
        logger.info("结束执行删除同步操作");
        return result;
    }
    //创建与更新文档时同步
    @Around("execution(* com.example.wsTextEditor.controller.DocumentsController.create*(..))||" +
            "execution(* com.example.wsTextEditor.controller.DocumentsController.update*(..))||"+
            "execution(* com.example.wsTextEditor.controller.DocumentsController.save*(..))")
    public Object createUpdateSyncDocument(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("开始执行创建与更新同步操作");
        Object result = joinPoint.proceed();
        Document document = documentRepository.findByUniqueId(joinPoint.getArgs()[0].toString())
                .orElse(null);
        documentSynService.syncDocument(document);
        logger.info("结束执行创建与更新同步操作");
        return result;
    }
}
