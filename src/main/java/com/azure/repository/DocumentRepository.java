package com.azure.repository;

import com.azure.model.document.Document;
import org.springframework.data.domain.Page;        // ★ 추가
import org.springframework.data.domain.Pageable; // ★ 추가
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    // ★ 변경: DB 페이징 + 연관경로(project.id) 명시
    Page<Document> findByProject_Id(Long projectId, Pageable pageable); // ★
}
