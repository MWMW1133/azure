package com.azure.service.impl;

import com.azure.model.document.Document;
import com.azure.model.document.DocumentVersion;
import com.azure.model.file.FileObject;
import com.azure.model.project.Project;
import com.azure.model.user.User;
import com.azure.repository.DocumentRepository;
import com.azure.repository.DocumentVersionRepository;
import com.azure.repository.FileObjectRepository;
import com.azure.repository.ProjectRepository;
import com.azure.service.file.DocumentService;
import com.azure.service.exception.BadRequestException; // 입력 검증/비즈니스 규칙 위반
import com.azure.service.exception.NotFoundException;   // 조회 대상 없음
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;              // [ADD]
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;                                                // [ADD]

/**
 * 문서/버전 도메인 서비스 구현.
 *
 * <h2>역할</h2>
 * - 컨트롤러와 레포지토리 사이의 비즈니스 규칙 캡슐화
 *   (입력 검증, FK 참조 묶기, 버전 번호 정책, 삭제 순서 등)
 * - 트랜잭션 경계 제공(쓰기: 기본 트랜잭션, 읽기: readOnly)
 *
 * <h2>설계 포인트</h2>
 * - DB 페이징 사용: DocumentRepository.findByProject_Id(projectId, pageable)
 * - 연관(FK) 세팅은 ID만 가진 얕은 엔티티로 연결하여 INSERT/UPDATE 최소화
 * - created_at/updated_at 은 DB가 관리 → 코드에서 값 세팅 안 함
 * - 문서 삭제 시 버전 먼저 정리(ON DELETE CASCADE가 없다면 필수)
 * - 버전 번호 자동 증가 + 중복 방지(서비스 레벨) ※ DB UNIQUE로 2차 방어 권장
 */
@Service
@Transactional
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    /** 문서 메타(프로젝트/제목/템플릿/작성자 등) CRUD */
    private final DocumentRepository documentRepository;
    /** 문서 파일 버전 이력 관리 */
    private final DocumentVersionRepository documentVersionRepository;
    /** 업로드 파일 메타(저장키/이름/MIME/크기 등) */
    private final FileObjectRepository fileObjectRepository;
    private final ProjectRepository projectRepository;
    private final EntityManager entityManager;


    // =========================================
    // [ADD] 최근 N개만 유지 보존 정책 (application.properties)
    // =========================================
    @Value("${documents.retain-versions:3}")
    private int retainVersions;

    /**
     * 단건 조회.
     * readOnly 트랜잭션으로 1차 캐시 및 불필요한 flush 방지.
     *
     * @throws NotFoundException 대상이 없으면 404 성격의 예외
     */
    @Override @Transactional(readOnly = true)
    public Document get(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found: " + id));
    }

    /**
     * 프로젝트별 문서 목록(페이지/정렬 포함).
     * - 레포에서 LIMIT/OFFSET 쿼리로 처리 → 대량 데이터에 안전.
     * - 정렬 키는 Pageable.sort 로 외부에서 주입(예: createdAt DESC).
     *
     * @throws BadRequestException projectId 미지정
     */
//    @Override @Transactional(readOnly = true)
//    public Page<Document> listByProject(Long projectId, Pageable pageable) {
//        if (projectId == null) throw new BadRequestException("projectId는 필수입니다.");
//        return documentRepository.findByProject_Id(projectId, pageable);
//    }
    @Override
    @Transactional(readOnly = true)
    public Page<Document> listByProject(Long projectId, Pageable pageable) {
        if (projectId == null) throw new BadRequestException("projectId는 필수입니다.");

        // [1] 문서 목록 조회 (author 함께)
        Page<Document> page = documentRepository.findByProject_Id(projectId, pageable);

        // [2] 각 문서별 최신 버전 → 파일 → 태스크 추적
        for (Document doc : page.getContent()) {
            try {
                var latestVersion = documentVersionRepository
                        .findTopByDocument_IdOrderByVersionNumDesc(doc.getId())
                        .orElse(null);

                if (latestVersion != null && latestVersion.getFile() != null) {
                    var file = latestVersion.getFile();

                    if (file.getTask() != null) {
                        // ⬇️ Document에 임시로 task를 붙임
                        doc.setTempTask(file.getTask());
                    }
                }

            } catch (Exception e) {
                // 혹시라도 Lazy 로딩 문제나 null 예외 나면 무시
                System.err.println("[WARN] Task 매핑 실패 (docId=" + doc.getId() + "): " + e.getMessage());
            }
        }

        return page;
    }



    /**
     * 문서 생성.
     * - FK(Project, Author)는 ID만 세팅한 엔티티로 연결(실제 SELECT 생략 가능).
     * - created_at/updated_at은 DB가 채움.
     *
     * @throws BadRequestException 필수값 누락/공백 제목
     */
    @Override
    public Document create(Long projectId, Long authorId, String title, String templateKey) {
        if (projectId == null) throw new BadRequestException("projectId는 필수입니다.");
        if (authorId == null) throw new BadRequestException("authorId는 필수입니다.");
        if (title == null || title.isBlank()) throw new BadRequestException("title은 필수입니다.");

        Document d = new Document();

//        Project project = new Project();
//        project.setId(projectId);
//        d.setProject(project);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BadRequestException("프로젝트를 찾을 수 없습니다."));
        d.setProject(project);

        User author = new User();
        author.setId(authorId);
        d.setAuthor(author);

        d.setTitle(title.trim());
        d.setTemplateKey(templateKey);

        Document saved = documentRepository.save(d);
        documentRepository.flush();
        return saved;
    }

    /**
     * 문서 메타데이터 수정(부분 업데이트).
     * - null이 아닌 값만 반영.
     */
    @Override
    public Document update(Long documentId, String title, String templateKey) {
        Document d = get(documentId);

        if (title != null) {
            String t = title.trim();
            if (t.isEmpty()) throw new BadRequestException("title은 빈 값일 수 없습니다.");
            if (t.length() > 200) throw new BadRequestException("title은 200자를 넘을 수 없습니다."); // [ADD]
            d.setTitle(t);
        }
        if (templateKey != null) {
            if (templateKey.length() > 100)
                throw new BadRequestException("templateKey는 100자를 넘을 수 없습니다.");           // [ADD]
            d.setTemplateKey(templateKey);
        }
        return documentRepository.save(d);
    }

    /**
     * 문서 삭제.
     * - 연결된 버전들을 먼저 제거(ON DELETE CASCADE 미사용 가정).
     */
    @Override
    public void delete(Long documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new NotFoundException("Document not found: " + documentId);
        }
        // [NOTE] DB에 CASCADE가 켜져 있으면 다음 줄은 생략 가능
        documentVersionRepository.deleteByDocument_Id(documentId);
        documentRepository.deleteById(documentId);
    }

    /**
     * 새 버전 추가.
     * - versionNum 미지정(null)이면 자동 증가(마지막 버전 + 1).
     * - 저장 후 [ADD] 보존 정책(enforceRetention) 적용.
     */
    @Override
    public DocumentVersion addVersion(Long documentId, Long fileId, Long authorId, Integer versionNum) {
        if (authorId == null) throw new BadRequestException("authorId는 필수입니다.");
        if (fileId == null)   throw new BadRequestException("fileId는 필수입니다.");

        Document doc = get(documentId);
        FileObject file = fileObjectRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found: " + fileId));

        if (versionNum == null) {
            int next = documentVersionRepository
                    .findTopByDocument_IdOrderByVersionNumDesc(documentId)
                    .map(v -> v.getVersionNum() + 1)
                    .orElse(1);
            versionNum = next;
        } else if (documentVersionRepository.existsByDocument_IdAndVersionNum(documentId, versionNum)) {
            throw new BadRequestException("이미 존재하는 버전 번호입니다: " + versionNum);
        }

        DocumentVersion v = new DocumentVersion();
        v.setDocument(doc);
        v.setFile(file);
        User authorRef = entityManager.getReference(User.class, authorId);
        v.setAuthor(authorRef);
        v.setVersionNum(versionNum);

        try {
            DocumentVersion saved = documentVersionRepository.save(v);
            enforceRetention(documentId);                                  // [ADD]
            return saved;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new BadRequestException("버전 번호 중복 또는 무결성 위반입니다.");
        }
    }

    // =========================
    // 버전 조회/롤백/삭제
    // =========================

    @Override @Transactional(readOnly = true)
    public Page<DocumentVersion> listVersions(Long documentId, Pageable pageable) {
        return documentVersionRepository.findByDocument_Id(documentId, pageable);
    }

    @Override @Transactional(readOnly = true)
    public DocumentVersion getVersion(Long documentId, Integer versionNum) {
        return documentVersionRepository
                .findByDocument_IdAndVersionNum(documentId, versionNum)
                .orElseThrow(() -> new NotFoundException("Version not found: v" + versionNum));
    }

    @Override @Transactional(readOnly = true)
    public DocumentVersion getLatestVersion(Long documentId) {
        return documentVersionRepository
                .findTopByDocument_IdOrderByVersionNumDesc(documentId)
                .orElseThrow(() -> new NotFoundException("No versions for document: " + documentId));
    }

    /**
     * [MOD] 롤백:
     *  - 복제 유틸 제거, 기존 파일(fileId) 재사용해서 새 버전으로 추가
     *  - 저장 후 보존 정책 자동 적용됨(addVersion 내부)
     */
    @Override
    public DocumentVersion rollback(Long documentId, Integer toVersion, Long actorId) {
        if (actorId == null) throw new BadRequestException("actorId는 필수입니다.");
        DocumentVersion base = getVersion(documentId, toVersion);
        if (base.getFile() == null) throw new BadRequestException("대상 버전에 파일이 없습니다.");
        return addVersion(documentId, base.getFile().getId(), actorId, null);     // [MOD]
    }

    /** 버전 삭제(마지막 1개 보호) */
    @Override
    public void deleteVersion(Long documentId, Integer versionNum) {
        long total = documentVersionRepository.countByDocument_Id(documentId);
        if (total <= 1) throw new BadRequestException("마지막 버전은 삭제할 수 없습니다.");

        DocumentVersion v = getVersion(documentId, versionNum);
        documentVersionRepository.delete(v);

        // [NOTE] 고아 FileObject 정리는 보수적으로 생략
        // (필요 시 repo 메서드 추가: existsByFile_Id(fileId) 로 참조 없을 때 삭제)
    }

    // =========================
    // [ADD] 최근 N개 보존 로직
    // =========================
    /**
     * 최근 retainVersions 개만 남기고 나머지(더 오래된) 버전들은 삭제한다.
     * - repo의 findByDocument_IdOrderByVersionNumDesc(...)만 사용하여 추가 쿼리 없이 처리
     * - 고아 FileObject 삭제는 안전을 위해 기본 비활성 (필요 시 별도 배치/메서드에서 처리)
     */
    private void enforceRetention(Long documentId) {
        if (retainVersions <= 0) return; // 0 이하이면 무제한 보존

        List<DocumentVersion> all = documentVersionRepository
                .findByDocument_IdOrderByVersionNumDesc(documentId);
        if (all.size() <= retainVersions) return;

        List<DocumentVersion> toDelete = all.subList(retainVersions, all.size());
        documentVersionRepository.deleteAll(toDelete);
        // [NOTE] 필요하면 여기서 고아 FileObject 정리 로직을 추가
    }
}
