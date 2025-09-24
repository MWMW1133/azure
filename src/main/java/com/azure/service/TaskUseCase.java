package com.azure.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public interface TaskUseCase {
    Long createTask(Long projectId, Long reporterId, String title);
    void updateProgress(Long taskId, BigDecimal pct);
    List<TaskSummary> listByProject(Long projectId);

    record TaskSummary(Long id, String title, BigDecimal progressPct) {}
}

@Service
@Transactional
class InMemoryTaskService implements TaskUseCase {
    private final AtomicLong seq = new AtomicLong(1);
    private final ConcurrentHashMap<Long, Task> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, List<Long>> byProject = new ConcurrentHashMap<>();

    static class Task {
        Long id, projectId, reporterId;
        String title;
        BigDecimal progressPct = BigDecimal.ZERO;
    }

    @Override
    public Long createTask(Long projectId, Long reporterId, String title) {
        Task t = new Task();
        t.id = seq.getAndIncrement();
        t.projectId = projectId;
        t.reporterId = reporterId;
        t.title = title;
        store.put(t.id, t);
        byProject.computeIfAbsent(projectId, k -> new CopyOnWriteArrayList<>()).add(t.id);
        return t.id;
    }

    @Override
    public void updateProgress(Long taskId, BigDecimal pct) {
        Task t = store.get(taskId);
        if (t == null) throw new IllegalArgumentException("no task");
        if (pct.compareTo(BigDecimal.ZERO) < 0 || pct.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("0..100");
        }
        t.progressPct = pct;
    }

    @Override
    public List<TaskSummary> listByProject(Long projectId) {
        return byProject.getOrDefault(projectId, List.of()).stream()
                .map(id -> store.get(id))
                .map(t -> new TaskSummary(t.id, t.title, t.progressPct))
                .toList();
    }
}