package com.azure.repository;

import com.azure.model.document.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    // (필요 시) 전체 버전 목록
    List<DocumentVersion> findByDocument_IdOrderByVersionNumDesc(Long documentId);

    // 최신 버전 1건 (자동 증가용)
    Optional<DocumentVersion> findTopByDocument_IdOrderByVersionNumDesc(Long documentId);

    // 특정 버전 존재 여부 (중복 방지)
    boolean existsByDocument_IdAndVersionNum(Long documentId, Integer versionNum);

    // 문서 삭제 전 버전 일괄 삭제 (CASCADE 없을 때 사용)
    long deleteByDocument_Id(Long documentId);
}
