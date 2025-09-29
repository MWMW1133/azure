package com.azure.repository;


import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.project.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> { 
         /** 하나의 조직에 속한 프로젝트들 페이징 조회 */
    Page<Project> findByOrganizationId(Long organizationId, Pageable pageable);
}
