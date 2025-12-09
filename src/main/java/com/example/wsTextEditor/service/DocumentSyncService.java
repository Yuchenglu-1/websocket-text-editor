package com.example.wsTextEditor.service;

import com.example.wsTextEditor.model.Document;
import com.example.wsTextEditor.model.DocumentSearchEntity;
import com.example.wsTextEditor.repository.DocumentRepository;
import com.example.wsTextEditor.repository.DocumentSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
//文档同步服务
@Service
public class DocumentSyncService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentSearchRepository searchableDocumentRepository;

    /**
     * 同步所有文档到 Elasticsearch
     */
    public void syncAllDocuments() {
        List<Document> documents = documentRepository.findAll();
        List<DocumentSearchEntity> searchableDocuments = documents.stream()
                .map(this::convertToSearchableDocument)
                .collect(Collectors.toList());

        searchableDocumentRepository.saveAll(searchableDocuments);
    }

    /**
     * 同步单个文档到 Elasticsearch
     */
    public void syncDocument(Document document) {
        DocumentSearchEntity searchableDocument = convertToSearchableDocument(document);
        searchableDocumentRepository.save(searchableDocument);
    }

    /**
     * 从 Elasticsearch 中删除文档
     */
    public void deleteDocumentFromIndex(Long documentId) {
        searchableDocumentRepository.deleteById(documentId);
    }

    /**
     * 将 Document 对象转换为 DocumentSearchEntity 对象
     */
    private DocumentSearchEntity convertToSearchableDocument(Document document) {
        DocumentSearchEntity entity = new DocumentSearchEntity();
        entity.setId(document.getId());
        entity.setTitle(document.getTitle());
        entity.setContent(document.getContent());
        entity.setLanguage(document.getLanguage());
        entity.setCreateAt(document.getCreatedAt());
        entity.setUpdateAt(document.getUpdatedAt());
        return entity;
    }
}