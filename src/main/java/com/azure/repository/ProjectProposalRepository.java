package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.project.ProjectProposal;

public interface ProjectProposalRepository extends JpaRepository<ProjectProposal, Long> { }
