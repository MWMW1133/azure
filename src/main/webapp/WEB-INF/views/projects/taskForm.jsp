<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- 
  태스크 입력 폼 템플릿
  - project-tab.js의 showTaskForm()에서 복제되어 사용됨
  - form ID는 클론 시 제거됨
--%>

<div class="task-form-row hidden" id="task-form-template" data-parent-id="">
  <div class="task-form-cell task-form-actions-cell">
    <span class="form-icon">📝</span>
  </div>

  <!-- 제목 -->
  <div class="task-form-cell task-form-title-cell">
    <input type="text" name="title" class="form-input" placeholder="새로운 태스크 이름" required />
  </div>

  <!-- 담당자 (초기엔 비어 있음 / 나중에 선택 기능 추가 가능) -->
  <div class="task-form-cell task-form-assignee-cell">
    <span class="assignee-placeholder">-</span>
  </div>

  <!-- 시작일 -->
  <div class="task-form-cell task-form-started-at-cell">
    <input type="date" name="startedAt" class="form-input" placeholder="시작일 선택" />
  </div>

  <!-- 마감일 -->
  <div class="task-form-cell task-form-duedate-cell">
    <input type="date" name="dueDate" class="form-input" placeholder="마감일 선택" />
  </div>

  <!-- 우선순위 -->
  <div class="task-form-cell task-form-priority-cell">
    <select name="priority" class="form-input">
      <option value="none" disabled selected>우선 순위</option>
      <option value="highest">매우 높음</option>
      <option value="high">높음</option>
      <option value="normal">보통</option>
      <option value="low">낮음</option>
      <option value="lowest">매우 낮음</option>
    </select>
  </div>

  <!-- 버튼 영역 -->
  <div class="task-form-cell task-form-updated-at-cell">
    <button type="button" class="btn-save js-save-task">저장</button>
    <button type="button" class="btn-cancel js-cancel-task">취소</button>
  </div>
</div>
