<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="currentTask" value="${task}" />

<div class="task-row"
     data-task-id="${currentTask.id}"
     data-assignee-id="${empty currentTask.assignee ? '' : currentTask.assignee.id}"
     data-assignee-name="${empty currentTask.assignee ? '' : currentTask.assignee.name}"
     data-assignee-avatar="${empty currentTask.assignee ? '' : currentTask.assignee.avatarUrl}"
     data-workflow-id="${empty currentTask.workflow ? '' : currentTask.workflow.id}"
     data-workflow-name="${empty currentTask.workflow ? '' : fn:escapeXml(currentTask.workflow.name)}"
     data-workflow-color="${empty currentTask.workflow ? '' : currentTask.workflow.color}"
     data-priority-id="${empty currentTask.priority ? '' : currentTask.priority.id}"
     data-priority-name="${empty currentTask.priority ? '' : fn:escapeXml(currentTask.priority.name)}"
     data-progress="${empty currentTask.progressPct ? 0 : currentTask.progressPct}">

  <!-- 선택 -->
  <div class="task-cell task-actions-cell">
    <div class="icon-wrapper"><input type="checkbox" class="form-check-input" /></div>
  </div>

  <!-- 제목 -->
  <div class="task-cell task-title-cell" data-cell="title">
    <span class="task-title-text">${fn:escapeXml(currentTask.title)}</span>
  </div>

  <!-- 담당자 -->
  <div class="task-cell assignee-cell" data-cell="assignee">
    <c:choose>
      <c:when test="${not empty currentTask.assignee}">
        <c:choose>
          <c:when test="${not empty currentTask.assignee.avatarUrl}">
            <img src="${currentTask.assignee.avatarUrl}" class="assignee-img"
                 alt="${fn:escapeXml(currentTask.assignee.name)}"/>
          </c:when>
          <c:otherwise>
            <div class="assignee-initial">
              <c:out value="${fn:length(currentTask.assignee.name) == 0 ? '?' : fn:substring(currentTask.assignee.name, 0, 1)}" />
            </div>
          </c:otherwise>
        </c:choose>
      </c:when>
      <c:otherwise>
        <div class="assignee-placeholder">-</div>
      </c:otherwise>
    </c:choose>
  </div>

  <!-- 시작일 -->
  <div class="task-cell started-at-cell" data-cell="startDate">
    <c:choose>
      <c:when test="${empty currentTask.startDate}">-</c:when>
      <c:otherwise><c:out value="${currentTask.startDate}" /></c:otherwise>
    </c:choose>
  </div>

  <!-- 마감일 -->
  <div class="task-cell duedate-cell" data-cell="dueDate">
    <c:choose>
      <c:when test="${empty currentTask.dueDate}">-</c:when>
      <c:otherwise><c:out value="${currentTask.dueDate}" /></c:otherwise>
    </c:choose>
  </div>

  <!-- 상태 -->
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
  </div>

  <!-- 우선순위 -->
  <div class="task-cell priority-cell" data-cell="priority">
    <span class="priority-badge"
          data-id="${empty currentTask.priority ? '' : currentTask.priority.id}"
          data-name="${empty currentTask.priority ? '' : fn:escapeXml(currentTask.priority.name)}">
      <span class="priority-dot" style="background:#e5e7eb;"></span>
      <span class="priority-text">
        <c:choose>
          <c:when test="${not empty currentTask.priority}">
            ${fn:escapeXml(currentTask.priority.name)}
          </c:when>
          <c:otherwise>-</c:otherwise>
        </c:choose>
      </span>
    </span>
  </div>

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
    </c:if>
  </div>

  <!-- 수정일 -->
  <div class="task-cell updated-at-cell" data-cell="updatedAt">
    <span class="updated-at-text">
      <c:choose>
        <c:when test="${empty currentTask.updatedAt}">-</c:when>
        <c:otherwise>
          <c:out value="${fn:substring(currentTask.updatedAt, 0, 10)}" />
        </c:otherwise>
      </c:choose>
    </span>
  </div>
</div>
