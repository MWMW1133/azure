package com.azure.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.azure.model.task.Task;
import java.util.List;
import java.util.Map;

public interface TaskRepository extends JpaRepository<Task, Long> {
    /** 특정 프로젝트의 모든 태스크 */
    @Query("SELECT t FROM Task t " +
       "LEFT JOIN FETCH t.assignee " +
       "LEFT JOIN FETCH t.workflow " +
       "LEFT JOIN FETCH t.priority " +
       "WHERE t.project.id = :projectId")
    List<Task> findByProjectId(@Param("projectId") Long projectId);

    /** 개인 태스크 조회 (project_id IS NULL) */
    Page<Task> findByProjectIdIsNullAndAssigneeId(Long userId, Pageable pageable);

    /** 특정 프로젝트 + 특정 담당자에 속한 태스크 */
    List<Task> findByProjectIdAndAssigneeId(Long projectId, Long assigneeId);

    /** 특정 담당자에 속한 모든 태스크 (개인 + 프로젝트) */
    Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);

    /** 특정 프로젝트 + 특정 워크플로우 단계에 속한 태스크 수 */
    long countByProjectIdAndWorkflow_Id(Long projectId, Long workflowId);

    /** 특정 프로젝트의 모든 태스크 (파일 포함) */
    @EntityGraph(attributePaths = {"assignee", "workflow", "priority", "files"})
    List<Task> findByProject_IdOrderByIdAsc(Long projectId);
    
    // 2-1) ID만 페이지로 뽑기
    @Query("""
    select t.id
    from Task t
    where t.project.id = :projectId
    order by t.id asc
    """)
    Page<Long> findIdsByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    // 2-2) 해당 ID들에 대해 To-One(assignee/workflow/priority)만 패치 조인
    @Query("""
    select distinct t
    from Task t
    left join fetch t.assignee
    left join fetch t.workflow
    left join fetch t.priority
    where t.id in :ids
    order by t.id asc
    """)
    List<Task> findByIdInFetchToOne(@Param("ids") List<Long> ids);

    // 2-3) 파일 개수 배치 조회용 인터페이스 & 쿼리
    public interface FileCount {
        Long getTaskId();
        long getCnt();
    }

    @Query("""
        select f.task.id as taskId, count(f.id) as cnt
        from FileObject f
        where f.task.id in :taskIds
        group by f.task.id
    """)
    List<FileCount> countFilesByTaskIds(@Param("taskIds") List<Long> taskIds);

    @EntityGraph(attributePaths = {"assignee", "workflow", "priority"})
    List<Task> findByProjectIdOrderByIdAsc(Long projectId);

    @EntityGraph(attributePaths = {"assignee", "workflow", "priority"})
    List<Task> findByParentTaskId(Long parentTaskId);

    @EntityGraph(attributePaths = {"assignee", "workflow", "priority"})
    Page<Task> findByProjectIdAndWorkflow_IdIn(Long projectId, List<Long> workflowIds, Pageable pageable);

     @EntityGraph(attributePaths = {"assignee", "workflow", "priority"})
    List<Task> findByProjectIdAndParentTaskIsNullOrderByIdAsc(Long projectId);

    @Query("""
        SELECT t.parentTask.id as parentId, COUNT(t.id) as cnt
        FROM Task t
        WHERE t.parentTask.id IN :parentIds
        GROUP BY t.parentTask.id
    """)
    List<Map<String, Object>> countChildrenByParentIds(@Param("parentIds") List<Long> parentIds);

    @EntityGraph(attributePaths = {"assignee", "workflow", "priority"})
    List<Task> findByProjectIdAndWorkflow_IsTerminalFalseOrderByIdAsc(Long projectId);

    @EntityGraph(attributePaths = {"assignee", "workflow", "priority"})
    List<Task> findByProjectIdAndWorkflow_IsTerminalTrueOrderByIdAsc(Long projectId);

    // ✅ Pageable -> Page<T> 로 수정
    @EntityGraph(attributePaths = {"assignee", "workflow", "priority"})
    Page<Task> findByProjectIdAndWorkflow_IsTerminalFalse(Long projectId, Pageable pageable);

    // ✅ Pageable -> Page<T> 로 수정
    @EntityGraph(attributePaths = {"assignee", "workflow", "priority"})
    Page<Task> findByProjectIdAndWorkflow_IsTerminalTrue(Long projectId, Pageable pageable);  
}