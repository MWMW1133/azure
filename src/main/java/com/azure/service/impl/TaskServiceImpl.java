package com.azure.service.impl;

import com.azure.dto.GanttTaskDTO;
import com.azure.event.TaskWorkflowChangedEvent;
import com.azure.model.file.FileObject;
import com.azure.model.task.*;
import com.azure.model.user.User;
import com.azure.model.workflow.Workflow;
import com.azure.repository.*;

import com.azure.service.TaskService;
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final FileObjectRepository fileObjectRepository;
    private final ApplicationEventPublisher publisher;

    @Override
    @Transactional(readOnly = true)
    public Task get(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> listByProject(Long projectId, Pageable pageable) {
        List<Task> all = taskRepository.findByProjectId(projectId);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<Task> content = (start > end) ? List.of() : all.subList(start, end);
        return new PageImpl<>(content, pageable, all.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> listPersonalTasks(Long userId, Pageable pageable) {
        return taskRepository.findByProjectIdIsNullAndAssigneeId(userId, pageable);
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

        // ★ 메서드명 수정
        return taskRepository.findByProjectIdAndWorkflow_IdIn(projectId, terminalWorkflowIds, pageable);
    }

    @Override
    public Map<Long, List<Task>> listTasksByAssignee(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream()
                .filter(t -> t.getAssignee() != null)
                .collect(Collectors.groupingBy(t -> t.getAssignee().getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GanttTaskDTO> getProjectTasksForGantt(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
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
    @Transactional
    public Task changeWorkflow(Long taskId, String toStage, Long actorUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        String fromStage = task.getWorkflow() != null ? task.getWorkflow().getName() : null;

        // ★ 이름으로 단계 찾기 → 엔티티 교체
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
    public Task create(Long projectId, Long assigneeId, String title, Long workflowId, Integer priorityId) {
        Task t = new Task();
        t.setTitle(title);

        t.setProject(new com.azure.model.project.Project());
        t.getProject().setId(projectId);

        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new NotFoundException("User not found: " + assigneeId));
            t.setAssignee(assignee);
        }
        if (workflowId != null) {
            Workflow w = workflowRepository.findById(workflowId)
                    .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowId));
            t.setWorkflow(w);
        }
        if (priorityId != null) {
            Priority pr = priorityRepository.findById(priorityId)
                    .orElseThrow(() -> new NotFoundException("Priority not found: " + priorityId));
            t.setPriority(pr);
        }
        return taskRepository.save(t);
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
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new NotFoundException("User not found: " + assigneeId));
        t.setAssignee(assignee);
        return taskRepository.save(t);
    }

    @Override
    public Task setWorkflow(Long taskId, Long workflowId) {
        Task task = get(taskId);
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowId));

        if (!workflow.getProject().getId().equals(task.getProject().getId())) {
            throw new IllegalArgumentException("Workflow does not belong to the same project as the task");
        }
        task.setWorkflow(workflow);
        return taskRepository.save(task);
    }

    @Override
    public Task setDates(Long taskId, LocalDate startDate, LocalDate dueDate) {
        Task t = get(taskId);
        t.setStartDate(startDate);
        t.setDueDate(dueDate);
        return taskRepository.save(t);
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

        TaskAttachment att = new TaskAttachment();
        TaskAttachmentId id = new TaskAttachmentId();
        id.setTaskId(task.getId());
        id.setFileId(file.getId());
        att.setId(id);
        att.setTask(task);
        att.setFile(file);
        taskAttachmentRepository.save(att);
    }

    @Override
    public void removeAttachment(Long taskId, Long fileId) {
        TaskAttachmentId id = new TaskAttachmentId();
        id.setTaskId(taskId);
        id.setFileId(fileId);
        taskAttachmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> listByAssignee(Long assigneeId, Pageable pageable) {
        return taskRepository.findByAssigneeId(assigneeId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByProjectAndWorkflow(Long projectId, Long workflowId) {
        return taskRepository.countByProjectIdAndWorkflow_Id(projectId, workflowId);
    }
}
