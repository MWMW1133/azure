<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%-- 현재 태스크 및 하위 태스크 데이터 바인딩 --%>
<c:set var="currentTask" value="${task}" />
<c:set var="children" value="${childrenByParent[currentTask.id]}" />

<div class="task-row" data-task-id="${currentTask.id}">
  <!-- 선택 및 토글 아이콘 -->
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
  <div class="task-cell task-title-cell">
    <span class="task-title-text">${fn:escapeXml(currentTask.title)}</span>
  </div>

  <%-- 담당자 안전 바인딩(서비스에서 미리 초기화됨) --%>
  <c:set var="assignee" value="${currentTask.assignee}" />
  <c:set var="assigneeName" value="${empty assignee ? '' : assignee.name}" />
  <c:set var="avatarUrl" value="${empty assignee ? null : assignee.avatarUrl}" />

  <!-- 담당자 -->
  <div class="task-cell assignee-cell">
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

    <!-- 담당자 선택 패널 (assignee-panel.js에서 제어됨) -->
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
  <div class="task-cell started-at-cell">
    <c:out value="${empty currentTask.startDate ? '-' : currentTask.startDate}" />
  </div>
  <div class="task-cell duedate-cell">
    <c:out value="${empty currentTask.dueDate ? '-' : currentTask.dueDate}" />
  </div>

  <!-- 상태(워크플로) -->
  <div class="task-cell status-cell">
    <c:set var="wfName" value="${empty currentTask.workflow ? 'Unspecified' : currentTask.workflow.name}" />
    <span class="status ${wfName}">${wfName}</span>
  </div>

  <!-- 우선순위 -->
  <div class="task-cell priority-cell">
    <c:choose>
      <c:when test="${currentTask.priority != null && currentTask.priority.name == 'highest'}">
        <span class="priority highest">매우 높음</span>
      </c:when>
      <c:when test="${currentTask.priority != null && currentTask.priority.name == 'high'}">
        <span class="priority high">높음</span>
      </c:when>
      <c:when test="${currentTask.priority != null && currentTask.priority.name == 'low'}">
        <span class="priority low">낮음</span>
      </c:when>
      <c:when test="${currentTask.priority != null && currentTask.priority.name == 'lowest'}">
        <span class="priority lowest">매우 낮음</span>
      </c:when>
      <c:otherwise>
        <span class="priority normal">보통</span>
      </c:otherwise>
    </c:choose>
  </div>

  <!-- 진행률 -->
  <div class="task-cell progress-cell">
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
  <div class="task-cell file-cell">
    <c:if test="${currentTask.hasFile}">
      <span class="file-icon"><i class="fa-solid fa-paperclip"></i></span>
    </c:if>
  </div>

  <!-- 수정일 -->
  <div class="task-cell updated-at-cell">
    <span class="updated-at-text">${empty currentTask.updatedAt ? '-' : currentTask.updatedAt}</span>
  </div>
</div>

