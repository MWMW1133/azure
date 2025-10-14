package com.azure.repository;

import com.azure.model.file.FileObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileObjectRepository extends JpaRepository<FileObject, Long> {

    // 조직(회사) 단위 목록: 최신 업로드 순
    List<FileObject> findByOrganization_IdOrderByCreatedAtDesc(Long organizationId);

    // 조직(회사) 단위 목록: 페이징
    Page<FileObject> findByOrganization_Id(Long organizationId, Pageable pageable);

    // 단건 + 조직 소속 검증
    Optional<FileObject> findByIdAndOrganization_Id(Long id, Long organizationId);
    boolean existsByIdAndOrganization_Id(Long id, Long organizationId);

    // (선택) 파일명 검색 + 페이징
    Page<FileObject> findByOrganization_IdAndFileNameContainingIgnoreCase(Long organizationId, String q, Pageable pageable);
}
