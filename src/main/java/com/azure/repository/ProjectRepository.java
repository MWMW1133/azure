package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.project.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> { }
