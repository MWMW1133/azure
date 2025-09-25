package com.azure.repository;

import com.azure.model.FileObjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileObjectRepository extends JpaRepository<FileObjectEntity, Long> {
}
