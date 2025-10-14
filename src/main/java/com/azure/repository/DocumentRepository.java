package com.azure.repository;

import com.azure.model.document.Document;
import com.azure.model.document.DocumentVersion;
import org.springframework.data.domain.Page;        // ★ 추가
import org.springframework.data.domain.Pageable; // ★ 추가
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @EntityGraph(attributePaths = {"author"})  // author를 함께 로딩
    Page<Document> findByProject_Id(Long projectId, Pageable pageable);

}
