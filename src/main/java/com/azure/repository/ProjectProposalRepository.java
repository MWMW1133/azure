package com.azure.repository;

import com.azure.model.ProjectProposalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectProposalRepository extends JpaRepository<ProjectProposalEntity, Long> {
}
