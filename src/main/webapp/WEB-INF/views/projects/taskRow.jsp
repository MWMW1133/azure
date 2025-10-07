<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="currentTask" value="${task}" />
<c:set var="children" value="${childrenByParent[currentTask.id]}" />

<div class="task-row" data-task-id="${currentTask.id}">
  <div class="task-cell task-actions-cell">
    <div class="icon-wrapper">
      <c:if test="${not empty children}">
        <span class="toggle-icon js-toggle-subtasks"><i class="fa-solid fa-caret-right"></i></span>
      </c:if>
      <c:if test="${empty children}">
        <input type="checkbox" class="form-check-input">
      </c:if>
    </div>
  </div>

  <div class="task-cell task-title-cell">${currentTask.title}</div>

  <!-- assignee 안전 접근 -->
  <div class="task-cell assignee-cell">
    <c:choose>
      <c:when test="${not empty currentTask.assignee and not empty currentTask.assignee.avatarUrl}">
        <img src="/images/${currentTask.assignee.avatarUrl}" class="assignee-img" alt="${currentTask.assignee.name}">
      </c:when>
      <c:otherwise>
        <div class="assignee-placeholder">-</div>
      </c:otherwise>
    </c:choose>

    <!-- 패널 DOM (데이터는 JS에서 로드) -->
    <div class="assignee-panel" role="dialog" aria-modal="true" aria-labelledby="assignee-title" hidden>
      <div class="assignee-search">
        <i class="bi bi-search"></i>
        <input type="text" class="assignee-search-input" placeholder="검색" aria-label="사용자 검색">
      </div>
      <ul class="assignee-list" aria-label="사용자 목록"></ul>
      <div class="assignee-footer">
        <button type="button" class="assignee-submit" disabled>
          <i class="bi bi-plus-lg"></i> 배정하기
        </button>
      </div>
    </div>
  </div>

  <div class="task-cell started-at-cell">${currentTask.startDate}</div>
  <div class="task-cell duedate-cell">${currentTask.dueDate}</div>

  <div class="task-cell status-cell">
    <c:set var="wfName" value="${empty currentTask.workflow ? 'Unspecified' : currentTask.workflow.name}" />
    <span class="status ${wfName}">${wfName}</span>
  </div>

  <div class="task-cell priority-cell">
    <c:choose>
      <c:when test="${not empty currentTask.priority and (currentTask.priority.name == 'highest' or currentTask.priority.name == 'very_high' or currentTask.priority.name == 'very-high')}">
        <span class="priority highest">매우 높음</span>
      </c:when>
      <c:when test="${not empty currentTask.priority and currentTask.priority.name == 'high'}">
        <span class="priority high">높음</span>
      </c:when>
      <c:when test="${not empty currentTask.priority and currentTask.priority.name == 'low'}">
        <span class="priority low">낮음</span>
      </c:when>
      <c:when test="${not empty currentTask.priority and (currentTask.priority.name == 'lowest' or currentTask.priority.name == 'very_low' or currentTask.priority.name == 'very-low')}">
        <span class="priority lowest">매우 낮음</span>
      </c:when>
      <c:otherwise>
        <span class="priority normal">보통</span>
      </c:otherwise>
    </c:choose>
  </div>

  <div class="task-cell progress-cell">
    <div class="progress-cell-wrapper">
      <span class="progress-value">${currentTask.progressPct}%</span>
      <div class="task-progress-container">
        <div class="task-progress-bar" data-progress="${currentTask.progressPct}"></div>
      </div>
    </div>
  </div>

  <div class="task-cell file-cell">
    <!--    <c:if test="${currentTask.hasFile}">
      <span class="file-icon"><i class="fa-solid fa-paperclip"></i></span>
    </c:if>-->
  </div>

  <div class="task-cell updated-at-cell">
    <span class="updated-at-text" style="cursor: pointer;">${currentTask.updatedAt}</span>
  </div>
</div>

<c:if test="${not empty children}">
  <div class="sub-task-container hidden">
    <div class="task-list-header sub-task-header">
      <div class="task-cell task-actions-cell"></div>
      <div class="task-cell task-title-cell">하위 태스크</div>
      <div class="task-cell assignee-cell">담당자</div>
      <div class="task-cell started-at-cell">시작일</div>
      <div class="task-cell duedate-cell">마감일</div>
      <div class="task-cell status-cell">상태</div>
      <div class="task-cell priority-cell">우선순위</div>
      <div class="task-cell progress-cell">진행률</div>
      <div class="task-cell file-cell">파일</div>
      <div class="task-cell updated-at-cell">최근 수정일</div>
    </div>

    <c:forEach var="subTaskItem" items="${children}">
      <c:set var="task" value="${subTaskItem}" scope="request" />
      <!-- ★ 절대경로로 자기 자신을 재귀 include -->
      <jsp:include page="/WEB-INF/views/projects/taskRow.jsp" />
    </c:forEach>
  </div>
</c:if>
