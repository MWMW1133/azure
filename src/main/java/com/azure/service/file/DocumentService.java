package com.azure.service.file;

import com.azure.model.document.Document;
import com.azure.model.document.DocumentVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 문서 및 버전 관리 서비스.
 * - 버전 번호 자동 증가: 최신 버전의 +1
 * - 보존 정책은 구현체에서 적용(예: 최근 N개만 유지)
 */
public interface DocumentService {
    /** ID로 문서 조회. */
    Document get(Long id);

    /** 프로젝트별 문서 목록(페이징). */
    Page<Document> listByProject(Long projectId, Pageable pageable);

    /** 문서 생성. */
    Document create(Long projectId, Long authorId, String title, String templateKey);

    /** 문서 메타데이터 수정. */
    Document update(Long documentId, String title, String templateKey);

    /** 문서 삭제. */
    void delete(Long documentId);

    /** 새 버전 추가(업로드된 FileObject와 연결). */
    DocumentVersion addVersion(Long documentId, Long fileId, Long authorId, Integer versionNum);

    // =======================
    // 버전 관련 확장 API
    // =======================

    /** 문서의 버전 목록(페이징). */
    Page<DocumentVersion> listVersions(Long documentId, Pageable pageable);

    /** 특정 버전 조회. */
    DocumentVersion getVersion(Long documentId, Integer versionNum);

    /** 최신 버전 조회. */
    DocumentVersion getLatestVersion(Long documentId);

    /**
     * 롤백: 과거 버전을 기준으로 "새 버전"을 생성하여 현재로 되돌린다.
     * (최근 N개 보존 정책 환경에서는 파일 복제 없이 동일 file을 재사용할 수 있음)
     */
    DocumentVersion rollback(Long documentId, Integer toVersion, Long actorId);

    /** 버전 삭제(마지막 1개 보호; 보존 정책과 별개로 수동 삭제 지원). */
    void deleteVersion(Long documentId, Integer versionNum);
}
