package com.azure.repository;

import com.azure.model.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, com.azure.model.ProjectMemberId> {}
