package com.azure.service;

import com.azure.model.task.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 태스크 CRUD 및 워크플로우 전이 규칙을 제공한다.
 * 담당자 변경, 칸반 단계 이동, 진행률 업데이트 등의 업무 규칙을 한 곳에서 관리한다.
 */
public interface TaskService {
    /** ID로 태스크 조회. 없으면 NotFoundException. */
    Task get(Long id);

    /**
     * 특정 프로젝트의 태스크 목록(페이징).
     * 레포지토리에 페이징 메서드가 없으면 서비스에서 List→Page로 감싼다.
     */
    Page<Task> listByProject(Long projectId, Pageable pageable);

    /** 최소 정보로 태스크 생성(제목/리포터/워크플로우/우선순위). 나머지는 후속 수정. */
    Task create(Long projectId, Long reporterId, String title, Long workflowId, Integer priorityId);

    /** 담당자 지정/해제(assigneeId가 null이면 해제). */
    Task assign(Long taskId, Long assigneeId);

    /** 다른 워크플로우(칸반 컬럼)로 이동. */
    Task moveToWorkflow(Long taskId, Long workflowId);

    /** 계획 시작일/마감일 설정. */
    Task setDates(Long taskId, LocalDate startDate, LocalDate dueDate);

    /** 진행률(0.00~100.00) 업데이트. */
    Task setProgress(Long taskId, BigDecimal progressPct);

    /** 태스크 삭제. */
    void delete(Long taskId);

    /** 첨부파일 추가/제거. */
    void addAttachment(Long taskId, Long fileId);
    void removeAttachment(Long taskId, Long fileId);
}
