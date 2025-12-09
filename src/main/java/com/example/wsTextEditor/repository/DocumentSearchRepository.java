package com.example.wsTextEditor.repository;

import com.example.wsTextEditor.model.DocumentSearchEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
//Es搜索仓库
@Repository
public interface DocumentSearchRepository extends ElasticsearchRepository<DocumentSearchEntity, Long> {
    //根据关键字搜索标题或内容进行搜索
    List<DocumentSearchEntity> findByTitleContainingOrContentContaining(String titleKeyword, String contentKeyword);
    //根据语言进行搜索
    List<DocumentSearchEntity> findByLanguage(String language);
    //根据语言和关键字进行搜索
    List<DocumentSearchEntity> findByTitleContainingAndLanguage(String titleKeyword, String language);
    //根据内容或语言进行搜索
    List<DocumentSearchEntity> findByContentContainingAndLanguage(String contentKeyword, String language);
    //根据UniqueId删除文档
    Optional<DocumentSearchEntity> findByUniqueId(String uniqueId);
    void deleteByUniqueId(String uniqueId);
}
