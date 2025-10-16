<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %> <%-- ★ JSTL 3.0 (Jakarta) --%>


<div class="main-wrapper">


  <div  class="main-wrapper-body"
        id="project-tab-root"             
        data-project-id="${projectId}"
        data-ctx="${pageContext.request.contextPath}">

    <!-- 진행중 -->
    <div class="active-task-container">
      <div class="active-container-header">
        <div class="container-title">진행중인 태스크</div>
        <div class="task-button-wrapper">
          <button class="task-btn" id="task-add-btn"><i class="bi bi-plus-lg"></i>태스크 추가</button>
          <button class="task-btn" id="task-delete-btn"><i class="bi bi-x-lg"></i>선택한 태스크 삭제</button>
        </div>
      </div>

      <div class="task-list-header">
        <div class="task-cell task-actions-cell"></div>
        <div class="task-cell task-title-cell">태스크</div>
        <div class="task-cell assignee-cell">담당자</div>
        <div class="task-cell started-at-cell">시작일</div>
        <div class="task-cell duedate-cell">마감일</div>
        <div class="task-cell status-cell">상태</div>
        <div class="task-cell priority-cell">우선순위</div>
        <div class="task-cell progress-cell">진행률</div>
        <div class="task-cell updated-at-cell">최근 수정일</div>
      </div>

      <div class="task-list-body">
        <c:if test="${not empty activeTasks}">  
          <c:forEach var="taskItem" items="${activeTasks}">
            <c:set var="task" value="${taskItem}" scope="request" />
            <jsp:include page="/WEB-INF/views/projects/taskRow.jsp" />
          </c:forEach>
        </c:if>
      </div>
    </div>

    <!-- 완료 -->
    <div class="archived-task-container">
      <div class="container-title">완료한 태스크</div>

      <div class="task-list-header">
        <div class="task-cell task-actions-cell"></div>
        <div class="task-cell task-title-cell">태스크</div>
        <div class="task-cell assignee-cell">담당자</div>
        <div class="task-cell started-at-cell">시작일</div>
        <div class="task-cell duedate-cell">마감일</div>
        <div class="task-cell status-cell">상태</div>
        <div class="task-cell priority-cell">우선순위</div>
        <div class="task-cell progress-cell">진행률</div>
        <div class="task-cell updated-at-cell">최근 수정일</div>
      </div>

      <div class="task-list-body">
        <c:if test="${not empty archivedTasks}"> 
          <c:forEach var="taskItem" items="${archivedTasks}">
            <c:set var="task" value="${taskItem}" scope="request" />
            <jsp:include page="/WEB-INF/views/projects/taskRow.jsp" />
          </c:forEach>
        </c:if>
      </div>
    </div>
  </div>
</div>

<!-- 새 태스크 추가 폼 -->
<div style="display: none;">
  <jsp:include page="/WEB-INF/views/projects/taskForm.jsp" />
</div>

<!-- 상태 변경 팝오버 -->
<div id="status-popover" class="status-popover" hidden>
  <div class="status-popover-body">
    <ul class="status-list"></ul>
    <div class="status-add-form">
      <i class="bi bi-plus-lg"></i>
      <input type="text" class="status-add-input" placeholder="새 상태 추가 + Enter">
    </div>
  </div>
  <div class="status-popover-footer">
    <button type="button" class="status-edit-btn">
      <i class="bi bi-gear-fill"></i> 상태 수정
    </button>
  </div>
</div>
<!-- 알림창 -->
<div class="toast-container position-fixed top-0 end-0 p-3" style="z-index:1080">
  <div id="planToast" class="toast clean-toast" role="status" aria-live="polite" aria-atomic="true">
    <div class="d-flex align-items-center gap-2">
      <span class="toast-icon" aria-hidden="true">ℹ</span>
      <div class="toast-body">완료되었습니다.</div>
      <button type="button" class="btn-close ms-auto" data-bs-dismiss="toast" aria-label="Close"></button>
    </div>
  </div>
</div>
<script>
  (function() {
    const root = document.getElementById('project-tab-root'); // ★ 위 id와 동일
    window.ProjectTab?.mount?.(root);
    window.AssigneePanel?.mount?.(root);
  })();
</script>
