package com.azure.repository;


import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.azure.model.project.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> { 
         /** 하나의 조직에 속한 프로젝트들 페이징 조회 */
    Page<Project> findByOrganizationId(Long organizationId, Pageable pageable);
    // 조직 + 이름 키워드로 페이징 조회
    Page<Project> findByOrganizationIdAndNameContaining(Long organizationId, String keyword, Pageable pageable);
    // 조직 내에서 프로젝트 이름 중복 체크
    Optional<Project> findByNameAndOrganizationId(String name, Long organizationId);

    Page<Project> findByOrganizationIdIn(List<Long> organizationIds, Pageable pageable);

    @Modifying
    @Query("update Project p set p.startDate = :start, p.dueDate = :due where p.id = :projectId")
    void updateDates(Long projectId, LocalDate start, LocalDate due);

}
