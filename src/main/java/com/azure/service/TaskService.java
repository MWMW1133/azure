package com.azure.service;

import com.azure.dto.GanttTaskDTO;
import com.azure.model.enums.PriorityCode;
import com.azure.model.task.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

    /** 개인 태스크 */
    Page<Task> listPersonalTasks(Long userId, Pageable pageable);

    /** 워크플로우의 마지막 단계(완료 상태)에 있는 태스크 리스트 (프로젝트별) */
    Page<Task> listCompletedTasksByProject(Long projectId, Pageable pageable);

    /** 프로젝트별 직원별 담당 태스크 리스트 */
    Map<Long, List<Task>> listTasksByAssignee(Long projectId);

    /** 프로젝트별 태스크 간트차트 데이터 */
    List<GanttTaskDTO> getProjectTasksForGantt(Long projectId);

    /** 프로젝트 태스크 생성 */
    Task createTask(Long projectId, Long assigneeId, String title, Long workflowId, Integer priorityId, LocalDate startDate, LocalDate dueDate, Long parentTaskId);

    /** 개인 태스크 생성 (project_id = null, assignee_id = 본인) */
    Task createPersonalTask(Long userId, String title, Integer priorityId);

    /** 하위 태스크 생성 (상위 태스크 ID 기준). */
    Task createSubTask(Long parentTaskId, Long assigneeId, String title, Long workflowId, Integer priorityId);

    /** 담당자 지정/해제(assigneeId가 null이면 해제). */
    Task assign(Long taskId, Long assigneeId);

    /** 다른 워크플로우(칸반 컬럼)로 이동. */
    Task setWorkflow(Long taskId, Long workflowId);

    //우선순위 변경
    Task setPriority(Long taskId, Long priorityId);

    /** 계획 시작일/마감일 설정. */
    Task setDates(Long taskId, LocalDate startDate, LocalDate dueDate);

    /** 진행률(0.00~100.00) 업데이트. */
    Task setProgress(Long taskId, BigDecimal progressPct);

    /** 태스크 삭제. */
    void delete(Long taskId);

    /** 첨부파일 추가/제거. */
    void addAttachment(Long taskId, Long fileId);
    void removeAttachment(Long taskId, Long fileId);

    /** 특정 담당자의 모든 태스크 목록(페이징) */
    Page<Task> listByAssignee(Long assigneeId, Pageable pageable);

    /** 특정 프로젝트 + 워크플로우에 속한 태스크 수 */
    long countByProjectAndWorkflow(Long projectId, Long workflowId);

    /** 특정 프로젝트의 모든 태스크 (List) */
    List<Task> listByProject(Long projectId);

    /** 태스크 생성(프로젝트 필수), 기본 워크플로 자동 채움 옵션 포함 */
    Task createTask(Long projectId, Long assigneeId, String title, Long workflowsId, Integer priorityId);

    /** 태스크 일괄 삭제 */
    void deleteTasks(List<Long> ids);

    /** 프로젝트의 모든 태스크(정렬) */
    List<Task> getTasksForProject(Long projectId);

    /** 워크플로 단계를 이름으로 변경(이벤트 발행 포함) */
    Task changeWorkflow(Long taskId, String toStage, Long actorUserId);

    List<Task> getSubTasks(Long parentId);
}
