package com.example.wsTextEditor.controller;

import com.example.wsTextEditor.model.DocumentSearchEntity;
import com.example.wsTextEditor.service.DocumentSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class DocumentSearchController {

    @Autowired
    private DocumentSearchService documentSearchService;

    /**
     * 根据关键字搜索文档
     * @param keyword 搜索关键字
     * @return 匹配的文档列表
     */
    @GetMapping("/documents")
    public ResponseEntity<?> searchDocuments(@RequestParam String keyword) {
        List<DocumentSearchEntity> results = documentSearchService.searchByKeyword(keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * 根据编程语言筛选文档
     * @param language 编程语言
     * @return 匹配的文档列表
     */
    @GetMapping("/documents/language")
    public ResponseEntity<?> searchDocumentsByLanguage(@RequestParam String language) {
        List<DocumentSearchEntity> results = documentSearchService.searchByLanguage(language);
        return ResponseEntity.ok(results);
    }

    /**
     * 根据关键字和编程语言组合搜索文档
     * @param keyword 搜索关键字
     * @param language 编程语言
     * @return 匹配的文档列表
     */
    @GetMapping("/documents/keyword-language")
    public ResponseEntity<?> searchDocumentsByKeywordAndLanguage(
            @RequestParam String keyword,
            @RequestParam String language) {
        List<DocumentSearchEntity> results = documentSearchService.searchByKeywordAndLanguage(keyword, language);
        return ResponseEntity.ok(results);
    }
}
