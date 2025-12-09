package com.example.wsTextEditor.service;

import com.example.wsTextEditor.model.DocumentSearchEntity;
import com.example.wsTextEditor.repository.DocumentSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentSearchService {

    @Autowired
    private DocumentSearchRepository documentSearchRepository;

    /**
     * 根据标题或内容关键字搜索文档
     * @param keyword 搜索关键字
     * @return 匹配的文档列表
     */
    public List<DocumentSearchEntity> searchByKeyword(String keyword) {
        return documentSearchRepository.findByTitleContainingOrContentContaining(keyword, keyword);
    }

    /**
     * 根据编程语言筛选文档
     * @param language 编程语言
     * @return 匹配的文档列表
     */
    public List<DocumentSearchEntity> searchByLanguage(String language) {
        return documentSearchRepository.findByLanguage(language);
    }

    /**
     * 根据关键字和编程语言组合搜索文档
     * @param keyword 搜索关键字
     * @param language 编程语言
     * @return 匹配的文档列表
     */
    public List<DocumentSearchEntity> searchByKeywordAndLanguage(String keyword, String language) {
        // 同时在标题和内容中搜索关键字
        List<DocumentSearchEntity> titleMatches = documentSearchRepository.findByTitleContainingAndLanguage(keyword, language);
        List<DocumentSearchEntity> contentMatches = documentSearchRepository.findByContentContainingAndLanguage(keyword, language);

        // 合并结果并去重
        titleMatches.addAll(contentMatches);
        return titleMatches.stream().distinct().toList();
    }

    /**
     * 保存文档到Elasticsearch索引中
     * @param documentSearchEntity 要保存的文档实体
     * @return 保存后的文档实体
     */
    public DocumentSearchEntity saveDocument(DocumentSearchEntity documentSearchEntity) {
        return documentSearchRepository.save(documentSearchEntity);
    }

    /**
     * 根据ID删除Elasticsearch中的文档
     * @param id 文档ID
     */
    public void deleteDocumentById(Long id) {
        documentSearchRepository.deleteById(id);
    }

}
