package com.azure.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data // Getter, Setter, toString 등을 자동으로 만들어주는 Lombok 어노테이션
public class Task {

    private Long id; // 각 태스크를 식별하기 위한 고유 ID
    private String title;
    private String assigneeName; // 담당자 이름
    private String assigneeImage; // 담당자 이미지 파일명
    private String startedAt;
    private String dueDate; // 마감일
    private String status; // 상태 (e.g., "in-progress", "completed")
    private String priority; // 우선순위 (e.g., "high", "normal")
    private boolean hasFile;
    private int processPct; // 진행률 (int 또는 double이 더 적합)
    private String updatedAt; // 최종 수정일

    // ★ 계층 구조를 위한 하위 태스크 목록
    private List<Task> subTasks;

    // 기본 생성자
    public Task() {
        this.subTasks = new ArrayList<>(); // NullPointerException 방지를 위해 초기화
    }

    // 모든 필드를 받는 생성자 (필요에 따라 만들어서 사용)
    public Task(Long id, String title, String assigneeName, String assigneeImage, String startedAt, String dueDate, String status, String priority, int processPct, boolean hasFile, String updatedAt) {
        this.id = id;
        this.title = title;

        this.assigneeName = assigneeName;
        this.assigneeImage = assigneeImage;
        this.startedAt = startedAt;
        this.dueDate = dueDate;
        this.status = status;
        this.priority = priority;
        this.hasFile = hasFile;
        this.processPct = processPct;
        this.updatedAt = updatedAt;
        this.subTasks = new ArrayList<>(); // 초기화
    }

    // 하위 태스크를 쉽게 추가하기 위한 편의 메소드
    public void addSubTask(Task subTask) {
        if (this.subTasks == null) {
            this.subTasks = new ArrayList<>();
        }
        this.subTasks.add(subTask);
    }
}