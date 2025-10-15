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
import com.azure.service.NotificationService;
import com.azure.service.TaskService;
import com.azure.service.UserService;
import com.azure.service.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final NotificationService notificationService;
    // =========================================================
    // 기본 조회
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public Task get(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found: " + id));
    }

    // =========================================================
    // 리스트 (페이징)
    // =========================================================
    /** 진행중(terminal=false)만 페이징 */
    @Override
    @Transactional(readOnly = true)
    public Page<Task> listByProject(Long projectId, Pageable pageable) {
        Page<Task> page = taskRepository.findByProjectIdAndWorkflow_IsTerminalFalse(projectId, pageable);
        prefetchToOne(page.getContent());
        return page;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> listPersonalTasks(Long userId, Pageable pageable) {
        Page<Task> page = taskRepository.findByProjectIdIsNullAndAssigneeId(userId, pageable);
        prefetchToOne(page.getContent());
        return page;
    }

    /** 완료(terminal=true) — 부모행만 페이지에 노출(자식은 펼침시 별도 호출) */
    @Override
    @Transactional(readOnly = true)
    public Page<Task> listCompletedTasksByProject(Long projectId, Pageable pageable) {
        Page<Task> page = taskRepository
                .findByProjectIdAndWorkflow_IsTerminalTrue(projectId, pageable);

        List<Task> parents = page.getContent().stream()
                .filter(t -> t.getParentTask() == null)
                .toList();

        if (!parents.isEmpty()) {
            List<Long> parentIds = parents.stream().map(Task::getId).toList();
            Map<Long, Long> childrenCountMap = taskRepository.countChildrenByParentIds(parentIds).stream()
                    .collect(Collectors.toMap(
                            map -> (Long) map.get("parentId"),
                            map -> (Long) map.get("cnt")
                    ));
            parents.forEach(p -> p.setChildrenCount(childrenCountMap.getOrDefault(p.getId(), 0L)));
        }

        prefetchToOne(parents);
        return new PageImpl<>(parents, pageable, parents.size());
    }

    // =========================================================
    // 그룹/검색
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<Task>> listTasksByAssignee(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        prefetchToOne(tasks);
        return tasks.stream()
                .filter(t -> t.getAssignee() != null)
                .collect(Collectors.groupingBy(t -> t.getAssignee().getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> listTasksByAssignee(Long assigneeId, Pageable pageable) {
        Page<Task> page = taskRepository.findByAssigneeId(assigneeId, pageable);
        prefetchToOne(page.getContent());
        return page;
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
    public List<GanttTaskDTO> getProjectTasksForGantt(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        prefetchToOne(tasks);
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

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksForProject(Long projectId) {
        List<Task> list = taskRepository.findByProject_IdOrderByIdAsc(projectId);
        prefetchToOne(list);
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getByProjectId(Long projectId) {
        List<Task> list = taskRepository.findByProjectIdOrderByIdAsc(projectId);
        prefetchToOne(list);
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getByProjectIdAndAssigneeId(Long projectId, Long assigneeId) {
        List<Task> list = taskRepository.findByProjectIdAndAssigneeId(projectId, assigneeId);
        prefetchToOne(list);
        return list;
    }

    // =========================================================
    // 상태/단계 변경
    // =========================================================
    /** 단계 이름으로 변경 + 이벤트 발행 */
    @Override
    public Task changeWorkflow(Long taskId, String toStage, Long actorUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        String fromStage = task.getWorkflow() != null ? task.getWorkflow().getName() : null;

        Workflow toWorkflow = workflowRepository
                .findByProjectIdAndName(task.getProject().getId(), toStage)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found in project: " + toStage));

        // 상위(부모) 테스크는 사용자 변경 금지
        if (!taskRepository.findByParentTaskId(taskId).isEmpty()) {
            throw new IllegalStateException("Parent task is controlled by its subtasks");
        }

        applyStageAndProgressRules(task, toWorkflow);
        taskRepository.save(task);

        if (task.getParentTask() != null) {
            updateParentAggregate(task.getParentTask().getId());
        }

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

    // =========================================================
    // 생성
    // =========================================================
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

        Workflow wf;
        if (workflowsId != null) {
            wf = workflowRepository.findById(workflowsId).orElse(null);
        } else {
            wf = workflowRepository.findByProjectIdAndIsDefaultTrue(projectId)
                    .or(() -> workflowRepository.findFirstByProject_IdOrderBySortOrderAsc(projectId))
                    .orElse(null);
        }
        task.setWorkflow(wf);

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
            task.setProject(parent.getProject());
        }

        // 생성 시 진행률 초기화
        if (task.getProgressPct() == null) {
            if (wf != null) {
                var stages = workflowRepository.findByProjectIdOrderBySortOrderAsc(projectId);
                int floor = computeStageFloorPct(stages, wf);
                task.setProgressPct(BigDecimal.valueOf(floor));
            } else {
                task.setProgressPct(BigDecimal.ZERO);
            }
        }

        Task saved = taskRepository.save(task);

        if (saved.getParentTask() != null) {
            updateParentAggregate(saved.getParentTask().getId());
        }
        return saved;
    }

    @Override
    public Task createTask(Long projectId, Long assigneeId, String title, Long workflowsId, Integer priorityId) {
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

        Workflow wf;
        if (workflowsId != null) {
            wf = workflowRepository.findById(workflowsId)
                    .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowsId));
        } else {
            wf = workflowRepository.findByProjectIdAndIsDefaultTrue(projectId)
                    .or(() -> workflowRepository.findFirstByProject_IdOrderBySortOrderAsc(projectId))
                    .orElse(null);
        }
        task.setWorkflow(wf);

        if (priorityId != null) {
            var pr = priorityRepository.findById(priorityId)
                    .orElseThrow(() -> new NotFoundException("Priority not found: " + priorityId));
            task.setPriority(pr);
        }

        // 생성 시 진행률 초기화
        if (task.getProgressPct() == null) {
            if (wf != null) {
                var stages = workflowRepository.findByProjectIdOrderBySortOrderAsc(projectId);
                int floor = computeStageFloorPct(stages, wf);
                task.setProgressPct(BigDecimal.valueOf(floor));
            } else {
                task.setProgressPct(BigDecimal.ZERO);
            }
        }

        Task saved = taskRepository.save(task);
        if (saved.getParentTask() != null) {
            updateParentAggregate(saved.getParentTask().getId());
        }
        return saved;
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
        if (t.getProgressPct() == null) t.setProgressPct(BigDecimal.ZERO);

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

        Workflow wf;
        if (workflowId != null) {
            wf = workflowRepository.findById(workflowId)
                    .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowId));
            if (!wf.getProject().getId().equals(parent.getProject().getId())) {
                throw new IllegalArgumentException("Workflow must belong to the same project as parent task");
            }
        } else {
            wf = workflowRepository.findByProjectIdAndIsDefaultTrue(parent.getProject().getId())
                    .or(() -> workflowRepository.findFirstByProject_IdOrderBySortOrderAsc(parent.getProject().getId()))
                    .orElse(null);
        }
        subTask.setWorkflow(wf);

        if (priorityId != null) {
            Priority pr = priorityRepository.findById(priorityId)
                    .orElseThrow(() -> new NotFoundException("Priority not found: " + priorityId));
            subTask.setPriority(pr);
        }

        // 생성 즉시 진행률 초기화
        if (wf != null) {
            var stages = workflowRepository.findByProjectIdOrderBySortOrderAsc(parent.getProject().getId());
            int floor = computeStageFloorPct(stages, wf);
            subTask.setProgressPct(BigDecimal.valueOf(floor));
        } else {
            subTask.setProgressPct(BigDecimal.ZERO);
        }

        Task saved = taskRepository.save(subTask);
        updateParentAggregate(parentTaskId);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getSubTasks(Long parentId) {
        List<Task> subTasks = taskRepository.findByParentTaskId(parentId);
        if (subTasks.isEmpty()) return subTasks;

        List<Long> parentIds = subTasks.stream().map(Task::getId).toList();
        Map<Long, Long> childrenCountMap = taskRepository.countChildrenByParentIds(parentIds).stream()
                .collect(Collectors.toMap(
                        map -> (Long) map.get("parentId"),
                        map -> (Long) map.get("cnt")
                ));
        subTasks.forEach(t -> t.setChildrenCount(childrenCountMap.getOrDefault(t.getId(), 0L)));

        prefetchToOne(subTasks);
        return subTasks;
    }

    // =========================================================
    // 변경
    // =========================================================
    @Override
    public Task assign(Long taskId, Long assigneeId, User actor) {
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
            actor,
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
    public Task setWorkflow(Long taskId, Long workflowId, User actor) {
        Task task = get(taskId);

        Long beforeId   = (task.getWorkflow()==null? null : task.getWorkflow().getId());
        String beforeNm = (task.getWorkflow()==null? null : task.getWorkflow().getName());
        String beforeCo = (task.getWorkflow()==null? null : task.getWorkflow().getColor());

        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowId));
        if (!workflow.getProject().getId().equals(task.getProject().getId())) {
            throw new IllegalArgumentException("Workflow does not belong to the same project as the task");
        }
        applyStageAndProgressRules(task, workflow);
        task.setWorkflow(workflow);
        Task saved = taskRepository.save(task);
        if (saved.getParentTask() != null) {
            updateParentAggregate(saved.getParentTask().getId());
        }
        Long afterId   = (saved.getWorkflow()==null? null : saved.getWorkflow().getId());
        String afterNm = (saved.getWorkflow()==null? null : saved.getWorkflow().getName());
        String afterCo = (saved.getWorkflow()==null? null : saved.getWorkflow().getColor());

        //로그남기기
        auditService.log(
            actor,
            com.azure.model.enums.AuditEnums.EntityType.TASK,
            saved.getId(),
            ActionType.WORKFLOW_CHANGED,
            new AuditDiff()
                .put("workflowId",   beforeId, afterId)
                .put("workflowName", beforeNm, afterNm)
                .put("workflowColor",beforeCo, afterCo)
        );

        //알??림
        ObjectMapper om = new ObjectMapper();
        Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("taskTitle", saved.getTitle());
        payload.put("from", beforeNm);
        payload.put("to", afterNm);
        payload.put("sender", actor != null ? actor.getName() : null);

        try {
            String json = om.writeValueAsString(payload);
            notificationService.notifyProjectMembers(saved.getProject().getId(), "TASK_WORKFLOW_CHANGED", json);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return saved;
    }

    @Override
    public Task setPriority(Long taskId, Long priorityId, User actor){
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
            actor,
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
    public Task setDates(Long taskId, LocalDate startDate, LocalDate dueDate, User actor) {
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
            actor,
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

        // 상위(부모) 테스크는 수동 변경 금지 – 하위 평균으로만 계산
        if (!taskRepository.findByParentTaskId(taskId).isEmpty()) {
            throw new IllegalStateException("Parent task progress is calculated from sub-tasks");
        }

        task.setProgressPct(progressPct);
        Task saved = taskRepository.save(task);

        if (task.getParentTask() != null) {
            updateParentAggregate(task.getParentTask().getId());
        }
        return saved;
    }

    @Override
    public void delete(Long taskId) {
        Task t = get(taskId);
        Long parentId = t.getParentTask() != null ? t.getParentTask().getId() : null;
        taskRepository.deleteById(taskId);
        if (parentId != null) {
            updateParentAggregate(parentId);
        }
    }

    @Override
    public void deleteTasks(List<Long> ids) {
        List<Long> parentIds = ids.stream()
                .map(id -> taskRepository.findById(id)
                        .map(t -> t.getParentTask() != null ? t.getParentTask().getId() : null)
                        .orElse(null))
                .filter(id -> id != null)
                .distinct()
                .toList();

        taskRepository.deleteAllById(ids);

        for (Long pid : parentIds) {
            updateParentAggregate(pid);
        }
    }

    // =========================================================
    // 파일
    // =========================================================
    @Override
    public void addAttachment(Long taskId, Long fileId) {
        Task task = get(taskId);
        FileObject file = fileObjectRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found: " + fileId));
        file.setTask(task);
        fileObjectRepository.save(file);
    }

    @Override
    public void removeAttachment(Long taskId, Long fileId) {
        FileObject file = fileObjectRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found: " + fileId));
        if (file.getTask() != null && file.getTask().getId().equals(taskId)) {
            String beforeName = file.getFileName();
            file.setTask(null);
            fileObjectRepository.save(file);
        }
    }

    // =========================================================
    // 카운트
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public long countByProjectAndWorkflow(Long projectId, Long workflowId) {
        return taskRepository.countByProjectIdAndWorkflow_Id(projectId, workflowId);
    }

    // =========================================================
    // 내부 로직
    // =========================================================
    /** 부모 집계: 하위 평균으로 진행률/단계 자동 결정(단계 수 늘어나도 동작) */
    private void updateParentAggregate(Long parentTaskId) {
        Task parent = get(parentTaskId);
        List<Task> subs = taskRepository.findByParentTaskId(parentTaskId);
        if (subs.isEmpty()) return;

        BigDecimal total = BigDecimal.ZERO;
        int cnt = 0;
        for (Task s : subs) {
            BigDecimal p = Optional.ofNullable(s.getProgressPct()).orElse(BigDecimal.ZERO);
            total = total.add(p);
            cnt++;
        }
        BigDecimal avg = (cnt > 0)
                ? total.divide(BigDecimal.valueOf(cnt), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<Workflow> stages = workflowRepository
                .findByProjectIdOrderBySortOrderAsc(parent.getProject().getId());
        if (stages.isEmpty()) {
            parent.setProgressPct(avg);
            taskRepository.save(parent);
            return;
        }

        Workflow picked = pickStageByProgress(stages, avg);
        parent.setWorkflow(picked);

        int snap = Boolean.TRUE.equals(picked.getIsTerminal())
                ? 100
                : Math.max(computeStageFloorPct(stages, picked) + (100 / (stages.size())),
                           computeStageFloorPct(stages, picked));
        if (snap >= 100 && !Boolean.TRUE.equals(picked.getIsTerminal())) snap = 99;
        parent.setProgressPct(BigDecimal.valueOf(snap));

        if (Boolean.TRUE.equals(picked.getIsTerminal())) {
            if (parent.getCompletedAt() == null) parent.setCompletedAt(LocalDateTime.now());
        } else {
            parent.setCompletedAt(null);
        }

        taskRepository.save(parent);

        if (parent.getParentTask() != null) {
            updateParentAggregate(parent.getParentTask().getId());
        }
    }

    /** 단계 이동 시(개별 태스크) 진행률/완료시간 규칙 */
    private void applyStageAndProgressRules(Task task, Workflow to) {
        task.setWorkflow(to);

        List<Workflow> stages = workflowRepository
                .findByProjectIdOrderBySortOrderAsc(task.getProject().getId());

        if (Boolean.TRUE.equals(to.getIsTerminal())) {
            if (task.getCompletedAt() == null) task.setCompletedAt(LocalDateTime.now());
            task.setProgressPct(BigDecimal.valueOf(100));
        } else {
            task.setCompletedAt(null);
            int floor = computeStageFloorPct(stages, to);
            BigDecimal floorBD = BigDecimal.valueOf(floor);
            BigDecimal cur = task.getProgressPct();
            if (cur == null || cur.compareTo(floorBD) < 0 || cur.compareTo(BigDecimal.valueOf(100)) == 0) {
                task.setProgressPct(floorBD);
            }
        }
    }

    /** 단계의 진행률 하한(floor). N단계면 step=100/N, i번째 floor=i*step (terminal은 별도 100 처리) */
    private int computeStageFloorPct(List<Workflow> orderedStages, Workflow stage) {
        int n = orderedStages.size();
        int idx = Math.max(0, orderedStages.indexOf(stage));
        int step = Math.max(1, 100 / n);
        if (Boolean.TRUE.equals(stage.getIsTerminal())) {
            return Math.max(0, 100 - step); // 표시용(실제 완료는 100)
        }
        return idx * step;
    }

    /** 평균으로 단계 선택(100이면 마지막) */
    private Workflow pickStageByProgress(List<Workflow> orderedStages, BigDecimal avgPct) {
        int n = orderedStages.size();
        int step = Math.max(1, 100 / n);

        if (avgPct != null && avgPct.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return orderedStages.get(n - 1);
        }
        int val = avgPct == null ? 0 : avgPct.intValue();
        int bucket = Math.min(n - 2, Math.max(0, val / step)); // 마지막은 100에서만
        return orderedStages.get(bucket);
    }

    /** To-One 연관 강제 초기화(JSP/JSON 접근 안전) */
    private void prefetchToOne(List<Task> tasks) {
        for (Task t : tasks) {
            if (t.getAssignee() != null) Hibernate.initialize(t.getAssignee());
            if (t.getWorkflow() != null) Hibernate.initialize(t.getWorkflow());
            if (t.getPriority() != null) Hibernate.initialize(t.getPriority());
        }
    }

    // =========================================================
    // 메인 테이블용 리스트(최상위 + 진행중만)
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public List<Task> listByProject(Long projectId) {
        List<Task> top = taskRepository.findByProjectIdAndParentTaskIsNullOrderByIdAsc(projectId);

        top = top.stream()
                .filter(t -> t.getWorkflow() == null
                        || !Boolean.TRUE.equals(t.getWorkflow().getIsTerminal()))
                .toList();

        if (top.isEmpty()) return top;

        List<Long> parentIds = top.stream().map(Task::getId).toList();
        Map<Long, Long> childrenCountMap = taskRepository.countChildrenByParentIds(parentIds).stream()
                .collect(Collectors.toMap(
                        map -> (Long) map.get("parentId"),
                        map -> (Long) map.get("cnt")
                ));
        top.forEach(t -> t.setChildrenCount(childrenCountMap.getOrDefault(t.getId(), 0L)));

        prefetchToOne(top);
        return top;
    }
}
