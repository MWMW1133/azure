package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.FileObject;

public interface FileObjectRepository extends JpaRepository<FileObject, Long> { }
