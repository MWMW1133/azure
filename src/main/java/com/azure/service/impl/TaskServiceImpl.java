package com.azure.service.impl;

import com.azure.dto.GanttTaskDTO;
import com.azure.dto.TaskUpdateDTO;
import com.azure.event.TaskWorkflowChangedEvent;
import com.azure.model.audit.AuditDiff;
import com.azure.model.enums.AuditEnums.ActionType;
import com.azure.model.enums.PriorityCode;
import com.azure.model.file.FileObject;
import com.azure.model.task.Priority;
import com.azure.model.task.Task;
import com.azure.model.user.User;
import com.azure.model.workflow.Workflow;
import com.azure.repository.FileObjectRepository;
import com.azure.repository.PriorityRepository;
import com.azure.repository.ProjectRepository;
import com.azure.repository.TaskRepository;
import com.azure.repository.UserRepository;
import com.azure.repository.WorkflowRepository;
import com.azure.security.SecurityUtil;
import com.azure.service.AuditService;
import com.azure.service.TaskService;
import com.azure.service.exception.NotFoundException;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final WorkflowRepository workflowRepository;
    private final PriorityRepository priorityRepository;
    private final UserRepository userRepository;
    private final FileObjectRepository fileObjectRepository;
    private final ApplicationEventPublisher publisher;
    private final ProjectRepository projectRepository;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public Task get(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> listByProject(Long projectId, Pageable pageable) {
        // 1) id만 페이징
        Page<Long> idPage = taskRepository.findIdsByProjectId(projectId, pageable);
        List<Long> ids = idPage.getContent();
        if (ids.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, idPage.getTotalElements());
        }

        // 2) To-One 연관만 패치해서 Task 엔티티 목록 조회
        List<Task> tasks = taskRepository.findByIdInFetchToOne(ids);

        // 3) 파일 개수 배치 조회 → 임시 필드에 주입
        var countMap = taskRepository.countFilesByTaskIds(ids).stream()
                .collect(Collectors.toMap(
                        TaskRepository.FileCount::getTaskId,
                        TaskRepository.FileCount::getCnt
                ));
        for (Task t : tasks) {
            t.setFileCount(countMap.getOrDefault(t.getId(), 0L));
        }

        // 🔒 안전: 혹시라도 프록시가 남아 있으면 초기화
        prefetchToOne(tasks);

        // 4) 원래 순서대로 재정렬
        var byId = tasks.stream().collect(Collectors.toMap(Task::getId, x -> x));
        List<Task> ordered = ids.stream().map(byId::get).toList();

        return new PageImpl<>(ordered, pageable, idPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> listPersonalTasks(Long userId, Pageable pageable) {
        Page<Task> page = taskRepository.findByProjectIdIsNullAndAssigneeId(userId, pageable);
        prefetchToOne(page.getContent());
        return page;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> listCompletedTasksByProject(Long projectId, Pageable pageable) {
        List<Long> terminalWorkflowIds = workflowRepository
                .findByProjectIdAndIsTerminalTrue(projectId)
                .stream()
                .map(Workflow::getId)
                .toList();

        if (terminalWorkflowIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // ✅ @EntityGraph가 붙은 레포 메서드 사용(전제) + 🔒보강 초기화
        Page<Task> page = taskRepository.findByProjectIdAndWorkflow_IdIn(projectId, terminalWorkflowIds, pageable);
        prefetchToOne(page.getContent());
        return page;
    }

    @Override
    public Map<Long, List<Task>> listTasksByAssignee(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        prefetchToOne(tasks);
        return tasks.stream()
                .filter(t -> t.getAssignee() != null)
                .collect(Collectors.groupingBy(t -> t.getAssignee().getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GanttTaskDTO> getProjectTasksForGantt(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        // DTO로 즉시 매핑하므로 Lazy 접근이 트랜잭션 안에서 끝남
        return tasks.stream()
                .map(t -> new GanttTaskDTO(
                        t.getId(),
                        t.getTitle(),
                        (t.getAssignee() != null ? t.getAssignee().getName() : null),
                        (t.getAssignee() != null ? t.getAssignee().getAvatarUrl() : null),
                        t.getStartDate(),
                        t.getDueDate(),
                        t.getProgressPct(),
                        (t.getWorkflow() != null ? t.getWorkflow().getName() : null)
                ))
                .toList();
    }

    /** 워크플로 단계 변경 */
    @Override
    public Task changeWorkflow(Long taskId, String toStage, Long actorUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        String fromStage = task.getWorkflow() != null ? task.getWorkflow().getName() : null;

        Workflow toWorkflow = workflowRepository
                .findByProjectIdAndName(task.getProject().getId(), toStage)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found in project: " + toStage));

        task.setWorkflow(toWorkflow);
        taskRepository.save(task);

        publisher.publishEvent(new TaskWorkflowChangedEvent(
                task.getProject().getId(),
                task.getId(),
                fromStage,
                toStage,
                actorUserId,
                task.getTitle()
        ));
        return task;
    }

    @Override
    public Task createTask(Long projectId, Long assigneeId, String title,
                       Long workflowsId, Integer priorityId,
                       LocalDate startDate, LocalDate dueDate,
                       Long parentTaskId) {
        var project = projectRepository.findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        Task task = new Task();
        task.setProject(project);
        task.setTitle(title);

        if (assigneeId != null) {
            var user = userRepository.findById(assigneeId)
                .orElseThrow(() -> new NotFoundException("User not found: " + assigneeId));
            task.setAssignee(user);
        }

        if (workflowsId != null) {
            var wf = workflowRepository.findById(workflowsId)
                .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowsId));
            task.setWorkflow(wf);
        } else {
            workflowRepository.findFirstByProject_IdOrderBySortOrderAsc(projectId)
                .ifPresent(task::setWorkflow);
        }

        if (priorityId != null) {
            var pr = priorityRepository.findById(priorityId)
                .orElseThrow(() -> new NotFoundException("Priority not found: " + priorityId));
            task.setPriority(pr);
        }

        task.setStartDate(startDate);
        task.setDueDate(dueDate);

        if (parentTaskId != null) {
            var parent = taskRepository.findById(parentTaskId)
                    .orElseThrow(() -> new NotFoundException("Parent task not found: " + parentTaskId));
                task.setParentTask(parent);
                task.setProject(parent.getProject()); // 부모와 동일 프로젝트 보장
            }

            return taskRepository.save(task);
    }

    @Override
    public Task createPersonalTask(Long userId, String title, Integer priorityId) {
        Task t = new Task();
        t.setTitle(title);
        t.setProject(null);

        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        t.setAssignee(assignee);

        t.setWorkflow(null);

        if (priorityId != null) {
            Priority pr = priorityRepository.findById(priorityId)
                    .orElseThrow(() -> new NotFoundException("Priority not found: " + priorityId));
            t.setPriority(pr);
        }
        return taskRepository.save(t);
    }

    @Override
    public Task createSubTask(Long parentTaskId, Long assigneeId, String title, Long workflowId, Integer priorityId) {
        Task parent = get(parentTaskId);
        Task subTask = new Task();
        subTask.setTitle(title);
        subTask.setProject(parent.getProject());
        subTask.setParentTask(parent);

        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new NotFoundException("User not found: " + assigneeId));
            subTask.setAssignee(assignee);
        }
        if (workflowId != null) {
            Workflow w = workflowRepository.findById(workflowId)
                    .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowId));
            if (!w.getProject().getId().equals(parent.getProject().getId())) {
                throw new IllegalArgumentException("Workflow must belong to the same project as parent task");
            }
            subTask.setWorkflow(w);
        }
        if (priorityId != null) {
            Priority pr = priorityRepository.findById(priorityId)
                    .orElseThrow(() -> new NotFoundException("Priority not found: " + priorityId));
            subTask.setPriority(pr);
        }
        return taskRepository.save(subTask);
    }

    @Override
    public Task assign(Long taskId, Long assigneeId) {
        Task t = get(taskId);

        Long beforeId   = (t.getAssignee()==null? null : t.getAssignee().getId());
        String beforeNm = (t.getAssignee()==null? null : t.getAssignee().getName());

        if (assigneeId == null) {
            t.setAssignee(null);
        } else {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new NotFoundException("User not found: " + assigneeId));
            t.setAssignee(assignee);
        }
        Task saved = taskRepository.save(t);

        Long afterId   = (saved.getAssignee()==null? null : saved.getAssignee().getId());
        String afterNm = (saved.getAssignee()==null? null : saved.getAssignee().getName());

        auditService.log(
            actor(),
            com.azure.model.enums.AuditEnums.EntityType.TASK,
            saved.getId(),
            ActionType.ASSIGNEE_CHANGED,
            new AuditDiff()
                .put("assigneeId",   beforeId, afterId)
                .put("assigneeName", beforeNm, afterNm)
        );
        return saved;
    }

    @Override
    public Task setWorkflow(Long taskId, Long workflowId) {
        Task task = get(taskId);

        Long beforeId   = (task.getWorkflow()==null? null : task.getWorkflow().getId());
        String beforeNm = (task.getWorkflow()==null? null : task.getWorkflow().getName());
        String beforeCo = (task.getWorkflow()==null? null : task.getWorkflow().getColor());

        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowId));
        if (!workflow.getProject().getId().equals(task.getProject().getId())) {
            throw new IllegalArgumentException("Workflow does not belong to the same project as the task");
        }
        task.setWorkflow(workflow);
        Task saved = taskRepository.save(task);

        Long afterId   = (saved.getWorkflow()==null? null : saved.getWorkflow().getId());
        String afterNm = (saved.getWorkflow()==null? null : saved.getWorkflow().getName());
        String afterCo = (saved.getWorkflow()==null? null : saved.getWorkflow().getColor());

        auditService.log(
            actor(),
            com.azure.model.enums.AuditEnums.EntityType.TASK,
            saved.getId(),
            ActionType.WORKFLOW_CHANGED,
            new AuditDiff()
                .put("workflowId",   beforeId, afterId)
                .put("workflowName", beforeNm, afterNm)
                .put("workflowColor",beforeCo, afterCo)
        );
        return saved;
    }

    @Override
    public Task setPriority(Long taskId, Long priorityId){
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + taskId));

        int beforeId   = (task.getPriority()==null? null : task.getPriority().getId());
        String beforeNm = (task.getPriority()==null? null : task.getPriority().getName());

        Priority priority = priorityRepository.findById(priorityId.intValue())
                .orElseThrow(() -> new EntityNotFoundException("Priority not found: " + priorityId));

        task.setPriority(priority);
        Task saved = taskRepository.save(task);

        int afterId   = (saved.getPriority()==null? null : saved.getPriority().getId());
        String afterNm = (saved.getPriority()==null? null : saved.getPriority().getName());

        auditService.log(
            actor(),
            com.azure.model.enums.AuditEnums.EntityType.TASK,
            saved.getId(),
            ActionType.PRIORITY_CHANGED,
            new AuditDiff()
                .put("priorityId",   beforeId, afterId)
                .put("priorityName", beforeNm, afterNm)
        );
        return saved;
    }

    @Override
    public Task setDates(Long taskId, LocalDate startDate, LocalDate dueDate) {
        Task t = get(taskId);

        // 변경 전 값 백업
        LocalDate beforeStart = t.getStartDate();
        LocalDate beforeDue   = t.getDueDate();

        // 변경
        t.setStartDate(startDate);
        t.setDueDate(dueDate);

        // 저장
        Task saved = taskRepository.save(t);

        // 감사 로그
        auditService.log(
            actor(),
            com.azure.model.enums.AuditEnums.EntityType.TASK,
            saved.getId(),
            ActionType.DATES_CHANGED,
            new AuditDiff()
                .put("startDate", beforeStart, saved.getStartDate())
                .put("dueDate",   beforeDue,   saved.getDueDate())
        );

        return saved;
    }


    @Override
    public Task setProgress(Long taskId, BigDecimal progressPct) {
        Task task = get(taskId);

        if (!taskRepository.findByParentTaskId(taskId).isEmpty()) {
            throw new IllegalArgumentException("Parent task progress is calculated from sub-tasks");
        }

        task.setProgressPct(progressPct);
        Task saved = taskRepository.save(task);

        if (task.getParentTask() != null) {
            updateParentProgress(task.getParentTask().getId());
        }
        return saved;
    }

    private void updateParentProgress(Long parentTaskId) {
        Task parent = get(parentTaskId);
        List<Task> subTasks = taskRepository.findByParentTaskId(parentTaskId);
        if (subTasks.isEmpty()) return;

        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (Task sub : subTasks) {
            if (sub.getProgressPct() != null) {
                total = total.add(sub.getProgressPct());
                count++;
            }
        }
        BigDecimal avg = (count > 0)
                ? total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        parent.setProgressPct(avg);
        taskRepository.save(parent);

        if (parent.getParentTask() != null) {
            updateParentProgress(parent.getParentTask().getId());
        }
    }

    @Override
    public void delete(Long taskId) {
        taskRepository.deleteById(taskId);
    }

    @Override
    public void addAttachment(Long taskId, Long fileId) {
        Task task = get(taskId);
        FileObject file = fileObjectRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found: " + fileId));
        file.setTask(task);
        fileObjectRepository.save(file);

        auditService.log(
            actor(),
            com.azure.model.enums.AuditEnums.EntityType.TASK,
            taskId,
            ActionType.FILE_ATTACHED,
            new AuditDiff()
                .put("fileId",   null, file.getId())
                .put("fileName", null, file.getFileName())
        );
    }

    @Override
    public void removeAttachment(Long taskId, Long fileId) {
        FileObject file = fileObjectRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found: " + fileId));
        if (file.getTask() != null && file.getTask().getId().equals(taskId)) {
            String beforeName = file.getFileName();
            file.setTask(null);
            fileObjectRepository.save(file);

            auditService.log(
                actor(),
                com.azure.model.enums.AuditEnums.EntityType.TASK,
                taskId,
                ActionType.FILE_REMOVED,
                new AuditDiff()
                    .put("fileId",   fileId, null)
                    .put("fileName", beforeName, null)
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksForProject(Long projectId) {
        List<Task> list = taskRepository.findByProjectIdOrderByIdAsc(projectId);
        prefetchToOne(list);
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> listByAssignee(Long assigneeId, Pageable pageable) {
        Page<Task> page = taskRepository.findByAssigneeId(assigneeId, pageable);
        prefetchToOne(page.getContent());
        return page;
    }

    @Override
    @Transactional(readOnly = true)
    public long countByProjectAndWorkflow(Long projectId, Long workflowId) {
        return taskRepository.countByProjectIdAndWorkflow_Id(projectId, workflowId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> listByProject(Long projectId) {
        // 1. 최상위 태스크만 조회
        List<Task> topLevelTasks = taskRepository.findByProjectIdAndParentTaskIsNullOrderByIdAsc(projectId);

        if (topLevelTasks.isEmpty()) {
            return topLevelTasks;
        }

        // 2. 하위 태스크 개수를 일괄 조회하기 위해 부모 ID 목록을 준비
        List<Long> parentIds = topLevelTasks.stream().map(Task::getId).collect(Collectors.toList());
        
        // 3. 한 번의 쿼리로 하위 태스크 개수들을 조회
        Map<Long, Long> childrenCountMap = taskRepository.countChildrenByParentIds(parentIds).stream()
                .collect(Collectors.toMap(
                        map -> (Long) map.get("parentId"),
                        map -> (Long) map.get("cnt")
                ));

        // 4. 각 최상위 태스크에 하위 태스크 개수를 설정
        topLevelTasks.forEach(task -> 
            task.setChildrenCount(childrenCountMap.getOrDefault(task.getId(), 0L))
        );

        prefetchToOne(topLevelTasks);
        return topLevelTasks;
    }

    @Transactional(readOnly = true)
    public List<Task> getSubTasks(Long parentId) {
        List<Task> subTasks = taskRepository.findByParentTaskId(parentId);
        
        if (subTasks.isEmpty()) {
            return subTasks;
        }

        // 중첩된 하위 태스크가 있을 수 있으므로, 이 하위 태스크들의 자식 개수도 조회합니다.
        List<Long> parentIds = subTasks.stream().map(Task::getId).collect(Collectors.toList());
        Map<Long, Long> childrenCountMap = taskRepository.countChildrenByParentIds(parentIds).stream()
                .collect(Collectors.toMap(
                        map -> (Long) map.get("parentId"),
                        map -> (Long) map.get("cnt")
                ));

        subTasks.forEach(task -> 
            task.setChildrenCount(childrenCountMap.getOrDefault(task.getId(), 0L))
        );
        
        prefetchToOne(subTasks);
        return subTasks;
    }

    @Override
    public Task createTask(Long projectId, Long assigneeId, String title, Long workflowsId, Integer priorityId) {
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        Task task = new Task();
        task.setProject(project);
        task.setTitle(title);

        // 담당자
        if (assigneeId != null) {
            var user = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new NotFoundException("User not found: " + assigneeId));
            task.setAssignee(user);
        }

        // 워크플로우 (단계)
        if (workflowsId != null) {
            var wf = workflowRepository.findById(workflowsId)
                    .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowsId));
            task.setWorkflow(wf);
        } else {
            workflowRepository.findFirstByProject_IdOrderBySortOrderAsc(projectId)
                    .ifPresent(task::setWorkflow);
        }

        // 우선순위
        if (priorityId != null) {
            var pr = priorityRepository.findById(priorityId)
                    .orElseThrow(() -> new NotFoundException("Priority not found: " + priorityId));
            task.setPriority(pr);
        }

        return taskRepository.save(task);
    }

    @Override
    public void deleteTasks(List<Long> ids) {
        taskRepository.deleteAllById(ids);
    }

    /** ------------------------- 내부 헬퍼 ------------------------- */
    /** JSP에서 접근하는 To-One 연관을 트랜잭션 내에서 강제 초기화 */
    private void prefetchToOne(List<Task> tasks) {
        for (Task t : tasks) {
            if (t.getAssignee() != null) Hibernate.initialize(t.getAssignee());
            if (t.getWorkflow() != null) Hibernate.initialize(t.getWorkflow());
            if (t.getPriority() != null) Hibernate.initialize(t.getPriority());
            // 파일 리스트가 꼭 필요하면 아래를 해제
            // if (t.getFiles() != null) Hibernate.initialize(t.getFiles());
        }
    }

    private User actor() {
       // Long id = SecurityUtil.getCurrentUserId(); 
       Long id = 1L;
        return userRepository.findById(id).orElse(null);
    }
}
