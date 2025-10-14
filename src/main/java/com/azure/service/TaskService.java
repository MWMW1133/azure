package com.azure.service;

import com.azure.dto.GanttTaskDTO;
import com.azure.model.enums.PriorityCode;
import com.azure.model.task.Task;
import com.azure.model.user.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 태스크 CRUD 및 워크플로우 전이 규칙을 제공한다.
 */
public interface TaskService {

    /** ID로 태스크 조회. 없으면 NotFoundException. */
    Task get(Long id);

    /** 특정 프로젝트의 태스크 목록(페이징) — 진행중(terminal=false)만 */
    Page<Task> listByProject(Long projectId, Pageable pageable);

    /** 개인 태스크 (project_id IS NULL) */
    Page<Task> listPersonalTasks(Long userId, Pageable pageable);

    /** 완료(terminal=true) 목록(페이징) — 하위 태스크도 포함해 모두 반환 */
    Page<Task> listCompletedTasksByProject(Long projectId, Pageable pageable);

    /** 프로젝트별 직원별 담당 태스크 리스트 */
    Map<Long, List<Task>> listTasksByAssignee(Long projectId);

    /** (옵션) 담당자별 목록(페이징) */
    Page<Task> listByAssignee(Long assigneeId, Pageable pageable);

    /** (옵션) 동일 의미: listTasksByAssignee */
    Page<Task> listTasksByAssignee(Long assigneeId, Pageable pageable);

    /** 프로젝트별 태스크 간트차트 데이터 */
    List<GanttTaskDTO> getProjectTasksForGantt(Long projectId);

    /** 프로젝트 태스크 생성(상세) */
    Task createTask(Long projectId, Long assigneeId, String title,
                    Long workflowId, Integer priorityId,
                    LocalDate startDate, LocalDate dueDate,
                    Long parentTaskId);

    /** 단순 생성 오버로드(날짜/부모 생략) */
    Task createTask(Long projectId, Long assigneeId, String title, Long workflowsId, Integer priorityId);

    /** 개인 태스크 생성 (project_id = null, assignee_id = 본인) */
    Task createPersonalTask(Long userId, String title, Integer priorityId);

    /** 하위 태스크 생성 (상위 태스크 ID 기준) */
    Task createSubTask(Long parentTaskId, Long assigneeId, String title, Long workflowId, Integer priorityId);


    //우선순위 변경
    Task setPriority(Long taskId, Long priorityId, User actor);

    /** 하위 태스크 조회 */
    List<Task> getSubTasks(Long parentId);

    /** 프로젝트의 모든 태스크(정렬) */
    List<Task> getTasksForProject(Long projectId);

    /** 프로젝트 내 특정 조건 단건/리스트 조회 (선택) */
    List<Task> getByProjectId(Long projectId);
    List<Task> getByProjectIdAndAssigneeId(Long projectId, Long assigneeId);

    /** 담당자 지정/해제(assigneeId가 null이면 해제) */
    Task assign(Long taskId, Long assigneeId, User actor);

    /** 워크플로우(칸반 컬럼) 변경 */
    Task setWorkflow(Long taskId, Long workflowId, User actor);

    /** 단계 이름으로 변경(이벤트 발행 포함) */
    Task changeWorkflow(Long taskId, String toStage, Long actorUserId);

    /** 계획 시작일/마감일 설정 */
    Task setDates(Long taskId, LocalDate startDate, LocalDate dueDate, User actor);

    /** 진행률(0.00~100.00) 업데이트 */
    Task setProgress(Long taskId, BigDecimal progressPct);

    /** 태스크 삭제(단건/일괄) */
    void delete(Long taskId);
    void deleteTasks(List<Long> ids);

    /** 특정 프로젝트 + 워크플로우에 속한 태스크 수 */
    long countByProjectAndWorkflow(Long projectId, Long workflowId);

    /** 메인 테이블용: 프로젝트의 최상위 ‘진행중’ 태스크 리스트 */
    List<Task> listByProject(Long projectId);
    
    /** 첨부파일 연결/해제 */
    void addAttachment(Long taskId, Long fileId, User actor);
    void removeAttachment(Long taskId, Long fileId, User actor);
}
