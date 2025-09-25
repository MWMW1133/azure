package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.DocumentVersion;
import java.util.List;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
    List<DocumentVersion> findByDocumentIdOrderByVersionNumDesc(Long documentId);
}
