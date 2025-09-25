package com.azure.repository;

import com.azure.model.DocumentVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersionEntity, Long> {
}
