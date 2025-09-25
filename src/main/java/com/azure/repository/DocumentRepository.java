package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.Document;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByProjectId(Long projectId);
}
