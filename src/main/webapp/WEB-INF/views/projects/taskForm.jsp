<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- 입력 폼 전용 JSP (클래스명 변경 버전) --%>
<div class="task-form-row" id="task-form-template">
    <div class="task-form-cell task-form-actions-cell">
    </div>
    <div class="task-form-cell task-form-title-cell">
        <input type="text" name="title" class="form-input" placeholder="새로운 태스크 이름" required>
    </div>
    <div class="task-form-cell task-form-assignee-cell">
        </div>
    <div class="task-form-cell task-form-started-at-cell">
        <input type="date" name="startedAt" class="form-input" placeholder="시작일 선택"> 
    </div>
    <div class="task-form-cell task-form-duedate-cell">
        <input type="date" name="dueDate" class="form-input" placeholder="마감일 선택">
    </div>
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

    <div class="task-form-cell task-form-updated-at-cell">
        <button type="button" class="btn-save js-save-task">저장</button>
        <button type="button" class="btn-cancel js-cancel-task">취소</button>
    </div>
</div>