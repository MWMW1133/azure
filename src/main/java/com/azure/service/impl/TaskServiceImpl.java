package com.azure.service.impl;

import com.azure.model.file.FileObject;
import com.azure.model.task.*;
import com.azure.model.user.User;
import com.azure.model.workflow.Workflow;
import com.azure.repository.*;
import com.azure.service.TaskService;
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 태스크 도메인 서비스 구현.
 * - 레포에 페이징 메서드가 부족한 경우 서비스에서 임시 페이징(PageImpl) 처리
 * - 성능 이슈가 생기면 레포에 findByProjectId(..., Pageable) 메서드를 추가하여 교체
 */
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

    /*ID로 태스크 조회. 없으면 NotFoundException.*/
    @Override @Transactional(readOnly = true)
    public Task get(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Task not found: " + id));
    }
    
    /*특정 프로젝트의 태스크 목록(페이징).*/
    @Override @Transactional(readOnly = true)
    public Page<Task> listByProject(Long projectId, Pageable pageable) {
        // 임시 페이징: List를 잘라 Page로 감싼다.
        List<Task> all = taskRepository.findByProjectId(projectId);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<Task> content = (start > end) ? List.of() : all.subList(start, end);
        return new PageImpl<>(content, pageable, all.size());
    }

    /*개인 태스크*/
    @Override
    @Transactional(readOnly = true)
    public Page<Task> listPersonalTasks(Long userId, Pageable pageable) {
        return taskRepository.findByProjectIdIsNullAndAssigneeId(userId, pageable);
    }

    /*최소 정보로 태스크 생성(제목/리포터/워크플로우/우선순위). 나머지는 후속 수정.*/
    @Override
    public Task create(Long projectId, Long assigneeId, String title, Long workflowId, Integer priorityId) {
        Task t = new Task();
        t.setTitle(title);

        // 프로젝트
        t.setProject(new com.azure.model.project.Project());
        t.getProject().setId(projectId);

        // 담당자 (assignee)
        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new NotFoundException("User not found: " + assigneeId));
            t.setAssignee(assignee);
        }

        // 워크플로우
        if (workflowId != null) {
            Workflow w = workflowRepository.findById(workflowId)
                    .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowId));
            t.setWorkflow(w);
        }

        // 우선순위
        if (priorityId != null) {
            Priority pr = priorityRepository.findById(priorityId)
                    .orElseThrow(() -> new NotFoundException("Priority not found: " + priorityId));
            t.setPriority(pr);
        }
        return taskRepository.save(t);
    }

    /*개인 태스크 생성 (project_id = null, assignee_id = 본인)*/
    @Override
    public Task createPersonalTask(Long userId, String title, Integer priorityId) {
        Task t = new Task();
        t.setTitle(title);

        // 프로젝트 없음 → 개인 태스크
        t.setProject(null);

        // 담당자는 본인
        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        t.setAssignee(assignee);

        // 워크플로우는 없음
        t.setWorkflow(null);

        // 우선순위
        if (priorityId != null) {
            Priority pr = priorityRepository.findById(priorityId)
                    .orElseThrow(() -> new NotFoundException("Priority not found: " + priorityId));
            t.setPriority(pr);
        }
        return taskRepository.save(t);
    }

    /*하위 태스크 생성 (상위 태스크 ID 기준).*/
    @Override
    public Task createSubTask(Long parentTaskId, Long assigneeId, String title, Long workflowId, Integer priorityId) {
        Task parent = get(parentTaskId);
        Task subTask = new Task();
        subTask.setTitle(title);
        subTask.setProject(parent.getProject()); // 같은 프로젝트
        subTask.setParentTask(parent);           // 상위 태스크 지정

        // 담당자
        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new NotFoundException("User not found: " + assigneeId));
            subTask.setAssignee(assignee);
        }

        // 워크플로우 (같은 프로젝트 검증)
        if (workflowId != null) {
            Workflow w = workflowRepository.findById(workflowId)
                    .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowId));
            if (!w.getProject().getId().equals(parent.getProject().getId())) {
                throw new IllegalArgumentException("Workflow must belong to the same project as parent task");
            }
            subTask.setWorkflow(w);
        }

        // 우선순위
        if (priorityId != null) {
            Priority pr = priorityRepository.findById(priorityId)
                    .orElseThrow(() -> new NotFoundException("Priority not found: " + priorityId));
            subTask.setPriority(pr);
        }
        return taskRepository.save(subTask);
    }

    /*담당자 지정/해제(assigneeId가 null이면 해제).*/
    @Override
    public Task assign(Long taskId, Long assigneeId) {
        Task t = get(taskId);
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new NotFoundException("User not found: " + assigneeId));
        t.setAssignee(assignee);
        return taskRepository.save(t);
    }

    /*워크플로우 설정*/
    @Override
    public Task setWorkflow(Long taskId, Long workflowId) {
        Task task = get(taskId);
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowId));

        // ✅ 프로젝트 일치 여부 검증
        if (!workflow.getProject().getId().equals(task.getProject().getId())) {
            throw new IllegalArgumentException("Workflow does not belong to the same project as the task");
        }

        task.setWorkflow(workflow);
        return taskRepository.save(task);
    }

    /*계획 시작일/마감일 설정.*/
    @Override
    public Task setDates(Long taskId, LocalDate startDate, LocalDate dueDate) {
        Task t = get(taskId);
        t.setStartDate(startDate);
        t.setDueDate(dueDate);
        return taskRepository.save(t);
    }

    /*진행률(0.00~100.00) 업데이트.*/
    @Override
    public Task setProgress(Long taskId, BigDecimal progressPct) {
        Task task = get(taskId);

        // ✅ 상위 태스크는 직접 설정 불가
        if (!taskRepository.findByParentTaskId(taskId).isEmpty()) {
            throw new IllegalArgumentException("Parent task progress is calculated from sub-tasks");
        }

        // 하위 태스크가 없는 경우 진행률 직접 설정 가능
        task.setProgressPct(progressPct);
        Task saved = taskRepository.save(task);

        // 상위 태스크 진행률 재귀 갱신
        if (task.getParentTask() != null) {
            updateParentProgress(task.getParentTask().getId());
        }
        return saved;
    }

        /** 상위 태스크 진행률을 하위 태스크들의 평균으로 갱신 */
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
                ? total.divide(BigDecimal.valueOf(count), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;

        parent.setProgressPct(avg);
        taskRepository.save(parent);

        // 상위-상위 태스크로 재귀 갱신
        if (parent.getParentTask() != null) {
            updateParentProgress(parent.getParentTask().getId());
        }
    }

    /*태스크 삭제.*/
    @Override
    public void delete(Long taskId) { taskRepository.deleteById(taskId); }

    /*첨부파일 추가/제거.*/
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
}
