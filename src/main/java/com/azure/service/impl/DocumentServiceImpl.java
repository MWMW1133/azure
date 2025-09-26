package com.azure.service.impl;

import com.azure.model.document.Document;
import com.azure.model.document.DocumentVersion;
import com.azure.model.file.FileObject;
import com.azure.model.project.Project;
import com.azure.model.user.User;
import com.azure.repository.DocumentRepository;
import com.azure.repository.DocumentVersionRepository;
import com.azure.repository.FileObjectRepository;
import com.azure.service.DocumentService;
import com.azure.service.exception.BadRequestException; // 입력 검증/비즈니스 규칙 위반
import com.azure.service.exception.NotFoundException;   // 조회 대상 없음
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Override @Transactional(readOnly = true)
    public Page<Document> listByProject(Long projectId, Pageable pageable) {
        if (projectId == null) throw new BadRequestException("projectId는 필수입니다.");
        return documentRepository.findByProject_Id(projectId, pageable);
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
        // 필수 파라미터 검증
        if (projectId == null) throw new BadRequestException("projectId는 필수입니다.");
        if (authorId == null) throw new BadRequestException("authorId는 필수입니다.");
        if (title == null || title.isBlank()) throw new BadRequestException("title은 필수입니다.");

        // 얕은 연관 세팅(영속성 컨텍스트에 프록시만 올라옴)
        Document d = new Document();
        d.setProject(new Project()); d.getProject().setId(projectId);
        d.setAuthor(new User());     d.getAuthor().setId(authorId);

        d.setTitle(title.trim());      // 공백 트리밍으로 일관성 유지
        d.setTemplateKey(templateKey); // 선택 필드

        return documentRepository.save(d);
    }

    /**
     * 문서 메타데이터 수정(부분 업데이트).
     * - null이 아닌 값만 반영.
     * - 제목은 공백 문자열 금지 + 트리밍.
     */
    @Override
    public Document update(Long documentId, String title, String templateKey) {
        Document d = get(documentId); // 존재 확인 겸 로딩

        if (title != null) {
            if (title.isBlank()) throw new BadRequestException("title은 빈 값일 수 없습니다.");
            d.setTitle(title.trim());
        }
        if (templateKey != null) {
            d.setTemplateKey(templateKey);
        }
        // updated_at 은 DB가 자동 갱신(ON UPDATE CURRENT_TIMESTAMP 등)
        return documentRepository.save(d);
    }

    /**
     * 문서 삭제.
     * - 사전 존재 확인으로 의미있는 예외 제공(NotFound).
     * - 연결된 버전들을 먼저 제거(ON DELETE CASCADE 미사용 가정).
     *   ※ 만약 DB에서 CASCADE가 설정되어 있다면 선삭제는 생략 가능.
     */
    @Override
    public void delete(Long documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new NotFoundException("Document not found: " + documentId);
        }
        // 문서-버전 관계가 CASCADE가 아니면 FK 제약 회피를 위해 선삭제 필요
        documentVersionRepository.deleteByDocument_Id(documentId);
        // 본문서 삭제
        documentRepository.deleteById(documentId);
    }

    /**
     * 새 버전 추가.
     * - fileId는 반드시 존재해야 함(파일 메타 선등록 전제).
     * - versionNum 미지정(null)이면 자동 증가(마지막 버전 + 1).
     * - versionNum 지정 시 중복 방지.
     *
     * <h3>동시성 주의</h3>
     * - 서비스 레벨에서 중복을 막아도, 동시 요청 레이스가 있으면 드물게 충돌 가능.
     *   → DB에 (document_id, version_num) UNIQUE 제약을 두고,
     *     DataIntegrityViolationException을 상위에서 잡아 메시지 변환하면 가장 안전.
     */
    @Override
    public DocumentVersion addVersion(Long documentId, Long fileId, Long authorId, Integer versionNum) {
        if (authorId == null) throw new BadRequestException("authorId는 필수입니다.");
        if (fileId == null)   throw new BadRequestException("fileId는 필수입니다.");

        // 참조 무결성(문서/파일 존재) 검사
        Document doc = get(documentId);
        FileObject file = fileObjectRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found: " + fileId));

        // 버전 번호 정책: null → 자동 증가, 지정 → 중복 검사
        if (versionNum == null) {
            int next = documentVersionRepository
                    .findTopByDocument_IdOrderByVersionNumDesc(documentId) // 최신 버전 조회
                    .map(v -> v.getVersionNum() + 1)                       // 다음 번호
                    .orElse(1);                                            // 최초 버전은 1
            versionNum = next;
        } else {
            boolean exists = documentVersionRepository
                    .existsByDocument_IdAndVersionNum(documentId, versionNum);
            if (exists) throw new BadRequestException("이미 존재하는 버전 번호입니다: " + versionNum);
        }

        // 엔티티 조립(연관은 ID만 세팅)
        DocumentVersion v = new DocumentVersion();
        v.setDocument(doc);
        v.setFile(file);
        v.setAuthor(new User()); v.getAuthor().setId(authorId);
        v.setVersionNum(versionNum);

        return documentVersionRepository.save(v);
    }
}
