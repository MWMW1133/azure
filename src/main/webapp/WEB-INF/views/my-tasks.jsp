<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<div class="wrapper-right-top">
  <div class="wrapper-title">나의 작업</div>
  <div class="wrapper-body">
    <div class="active-task-container">
      <div class="container-title">진행중인 태스크</div>
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
        <c:forEach var="taskItem" items="${activeTasks}">
          <c:set var="task" value="${taskItem}" scope="request" />
          <jsp:include page="/WEB-INF/views/tasks/taskRow.jsp" />
        </c:forEach>
      </div>
    </div>
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
        <c:forEach var="taskItem" items="${archivedTasks}">
          <c:set var="task" value="${taskItem}" scope="request" />
          <jsp:include page="/WEB-INF/views/tasks/taskRow.jsp" />
        </c:forEach>
      </div>
    </div>
  </div>
</div>
