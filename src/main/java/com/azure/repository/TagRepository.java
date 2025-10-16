package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.azure.model.tag.Tag;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    // 특정 프로젝트에 속한 모든 태그 조회
    List<Tag> findByProjectId(Long projectId);
    // 특정 프로젝트 내에서 태그 이름으로 조회
    Optional<Tag> findByProjectIdAndName(Long projectId, String name);
    // 특정 프로젝트 내에서 태그 이름 중복 체크
    boolean existsByProjectIdAndName(Long projectId, String name);

    @Modifying
    @Transactional
    @Query("delete from Tag t where t.project.id = :projectId")
    void deleteByProjectId(Long projectId);
}
