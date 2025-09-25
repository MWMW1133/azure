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

    @Override @Transactional(readOnly = true)
    public Task get(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Task not found: " + id));
    }

    @Override @Transactional(readOnly = true)
    public Page<Task> listByProject(Long projectId, Pageable pageable) {
        // 임시 페이징: List를 잘라 Page로 감싼다.
        List<Task> all = taskRepository.findByProjectId(projectId);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<Task> content = (start > end) ? List.of() : all.subList(start, end);
        return new PageImpl<>(content, pageable, all.size());
    }

    @Override
    public Task create(Long projectId, Long reporterId, String title, Long workflowId, Integer priorityId) {
        Task t = new Task();
        t.setTitle(title);
        // 관계는 ID만 세팅해도 JPA가 FK로 인식
        t.setProject(new com.azure.model.project.Project()); t.getProject().setId(projectId);
        if (reporterId != null) { t.setReporter(new User()); t.getReporter().setId(reporterId); }
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
    public Task assign(Long taskId, Long assigneeId) {
        Task t = get(taskId);
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new NotFoundException("User not found: " + assigneeId));
        t.setAssignee(assignee);
        return taskRepository.save(t);
    }

    @Override
    public Task moveToWorkflow(Long taskId, Long workflowId) {
        Task t = get(taskId);
        Workflow w = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowId));
        t.setWorkflow(w);
        return taskRepository.save(t);
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
        Task t = get(taskId);
        t.setProgressPct(progressPct);
        return taskRepository.save(t);
    }

    @Override
    public void delete(Long taskId) { taskRepository.deleteById(taskId); }

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
