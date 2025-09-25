package com.azure.service;

import com.azure.model.document.Document;
import com.azure.model.document.DocumentVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 문서 및 버전 관리 기능을 제공한다.
 * 버전 번호 자동 증가가 필요하면 최대값+1 로직을 여기에 추가할 수 있다.
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
}
