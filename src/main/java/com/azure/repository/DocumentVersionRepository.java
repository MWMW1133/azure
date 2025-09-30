package com.azure.repository;

import com.azure.model.document.DocumentVersion;
import org.springframework.data.domain.Page;        // [ADD]
import org.springframework.data.domain.Pageable; // [ADD]
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    // (필요 시) 전체 버전 목록
    List<DocumentVersion> findByDocument_IdOrderByVersionNumDesc(Long documentId);

    // [ADD] 페이징 버전 목록
    Page<DocumentVersion> findByDocument_Id(Long documentId, Pageable pageable);

    // 최신 버전 1건 (자동 증가용)
    Optional<DocumentVersion> findTopByDocument_IdOrderByVersionNumDesc(Long documentId);

    // [ADD] 특정 버전 단건 조회
    Optional<DocumentVersion> findByDocument_IdAndVersionNum(Long documentId, Integer versionNum);

    // 특정 버전 존재 여부 (중복 방지)
    boolean existsByDocument_IdAndVersionNum(Long documentId, Integer versionNum);

    // [ADD] 문서별 버전 개수(마지막 버전 보호 규칙)
    long countByDocument_Id(Long documentId);

    // 문서 삭제 전 버전 일괄 삭제 (CASCADE 없을 때 사용)
    long deleteByDocument_Id(Long documentId);

    // keepMin 미만(더 오래된) 버전 싹 정리용
    List<DocumentVersion> findByDocument_IdAndVersionNumLessThan(Long documentId, Integer versionNum);

    // FileObject 고아 여부 판단용
    boolean existsByFile_Id(Long fileId);
}
