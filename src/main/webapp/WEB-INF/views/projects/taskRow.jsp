<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%-- 현재 태스크 및 하위 태스크 데이터 바인딩 --%>
<c:set var="currentTask" value="${task}" />
<c:set var="children" value="${childrenByParent[currentTask.id]}" />

<%-- 행에 현재 값들을 data-*로 싣고, 셀은 JS가 채우게 둔다 --%>
<div class="task-row"
     data-task-id="${currentTask.id}"
     data-assignee-id="${currentTask.assignee != null ? currentTask.assignee.id : ''}"
     data-workflow-id="${empty currentTask.workflow ? '' : currentTask.workflow.id}"
     data-workflow-name="${empty currentTask.workflow ? '' : fn:escapeXml(currentTask.workflow.name)}"
     data-workflow-color="${empty currentTask.workflow ? '' : currentTask.workflow.color}"
     data-priority-id="${empty currentTask.priority ? '' : currentTask.priority.id}"
     data-progress="${empty currentTask.progressPct ? 0 : currentTask.progressPct}"
>
  <!-- 선택/토글 -->
  <div class="task-cell task-actions-cell">
    <div class="icon-wrapper">
      <c:choose>
        <c:when test="${task.childrenCount > 0}">
          <span class="toggle-icon js-toggle-subtasks"><i class="fa-solid fa-caret-right"></i></span>
        </c:when>
        <c:otherwise>
          <input type="checkbox" class="form-check-input" />
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <!-- 제목 -->
  <div class="task-cell task-title-cell" data-cell="title">
    <span class="task-title-text">${fn:escapeXml(currentTask.title)}</span>
  </div>

  <%-- 담당자 안전 바인딩 --%>
  <c:set var="assignee" value="${currentTask.assignee}" />
  <c:set var="assigneeName" value="${empty assignee ? '' : assignee.name}" />
  <c:set var="avatarUrl" value="${empty assignee ? null : assignee.avatarUrl}" />

  <!-- 담당자 (패널 포함: 기존 유지) -->
  <div class="task-cell assignee-cell" data-cell="assignee">
    <c:choose>
      <c:when test="${not empty assignee}">
        <c:choose>
          <c:when test="${not empty avatarUrl}">
            <img src="${avatarUrl}" class="assignee-img" alt="${fn:escapeXml(assigneeName)}" />
          </c:when>
          <c:otherwise>
            <div class="assignee-initial">
              <c:out value="${fn:length(assigneeName) == 0 ? '?' : fn:substring(assigneeName, 0, 1)}" />
            </div>
          </c:otherwise>
        </c:choose>
      </c:when>
      <c:otherwise>
        <div class="assignee-placeholder">-</div>
      </c:otherwise>
    </c:choose>

    <div class="assignee-panel" role="dialog" aria-modal="true" hidden>
      <div class="assignee-search">
        <i class="bi bi-search"></i>
        <input type="text" class="assignee-search-input" placeholder="검색" aria-label="사용자 검색" />
      </div>
      <ul class="assignee-list" aria-label="사용자 목록"></ul>
      <div class="assignee-footer">
        <button type="button" class="assignee-submit" disabled>
          <i class="bi bi-plus-lg"></i> 배정하기
        </button>
      </div>
    </div>
  </div>

  <!-- 시작일 / 마감일 -->
  <div class="task-cell started-at-cell" data-cell="startDate">
    <c:out value="${empty currentTask.startDate ? '-' : currentTask.startDate}" />
  </div>
  <div class="task-cell duedate-cell" data-cell="dueDate">
    <c:out value="${empty currentTask.dueDate ? '-' : currentTask.dueDate}" />
  </div>

  <!-- 상태(워크플로) : 내용은 비워두고 JS가 채움 -->
  <div class="task-cell status-cell" data-cell="workflow">
    <c:choose>
      <c:when test="${not empty currentTask.workflow}">
        <span class="status-badge">
          <span class="status-dot" style="background:${currentTask.workflow.color};"></span>
          <span class="status-text">${fn:escapeXml(currentTask.workflow.name)}</span>
        </span>
      </c:when>
      <c:otherwise>
        <span class="status-badge">
          <span class="status-dot" style="background:#e5e7eb;"></span>
          <span class="status-text">-</span>
        </span>
      </c:otherwise>
    </c:choose>

    <!-- 상태 패널 (숨김, JS가 열고 닫음) -->
    <div class="status-panel" role="dialog" aria-modal="true" hidden>
      <div class="status-search">
        <i class="bi bi-search"></i>
        <input type="text" class="status-search-input" placeholder="상태 검색" />
      </div>
      <ul class="status-list"></ul>
      <div class="status-footer">
        <input class="status-new-name" type="text" placeholder="새 상태명" />
        <input class="status-new-color" type="color" value="#ffffff" />
        <button class="status-create-btn" type="button">추가</button>
      </div>
    </div>
  </div>

  <!-- 우선순위 : 내용은 비워두고 JS가 채움 -->
  <div class="task-cell priority-cell" data-cell="priority"></div>

  <!-- 진행률 -->
  <div class="task-cell progress-cell" data-cell="progress">
    <div class="progress-cell-wrapper">
      <span class="progress-value">
        <c:out value="${empty currentTask.progressPct ? 0 : currentTask.progressPct}" />%
      </span>
      <div class="task-progress-container">
        <div class="task-progress-bar" data-progress="${empty currentTask.progressPct ? 0 : currentTask.progressPct}"></div>
      </div>
    </div>
  </div>

  <!-- 파일 -->
  <div class="task-cell file-cell" data-cell="files">
    <c:if test="${currentTask.hasFile}">
      <span class="file-icon"><i class="fa-solid fa-paperclip"></i></span>
    </c:if>
  </div>

  <!-- 수정일 -->
  <div class="task-cell updated-at-cell" data-cell="updatedAt">
    <span class="updated-at-text">${empty currentTask.updatedAt ? '-' : currentTask.updatedAt}</span>
  </div>
</div>
