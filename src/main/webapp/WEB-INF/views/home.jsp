
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<div class="container">
  <div class="container-body">
    <div class="container-body-top">
      <h2>안녕하세요, ${user.name}님.</h2>
    </div>
    <div class="kanban-board">
       <!-- TO DO -->
      <div class="kanban-column">
        <div class="column-title">TO DO</div>
        <div class="card-list">
          <c:forEach var="item" items="${todoList}">
            <div class="card">
              <h3 class="card-title">${item.title}</h3>
              <p class="card-description">${item.description}</p>
              <div class="card-bottom">
                <span class="card-todo-date">${item.time}</span>
                <span class="card-todo-status ${item.status}">${item.status}</span>
              </div>
            </div>
          </c:forEach>
          <c:if test="${empty todoList}">
            <div class="text-muted small px-2 py-3">오늘 표시할 TO-DO가 없습니다.</div>
          </c:if>
        </div>

      <!-- 마감일 며칠 안 남은 거 표시 -->
      <div class="kanban-column">
        <div class="column-title">마감일 임박</div>
        <div class="card-list">
          <c:forEach var="task" items="${inprogressTasks}">
            <div class="card">
              <h3 class="card-title">${task.title}</h3>
              <p class="dueDate">${task.dueDate}</p>
              <div class="card-bottom">
                <span class="card-task-status" style="background-color: ${task.workflowColor}">${task.workflow}</span>
                <c:choose>
                  <c:when test="${task.priority == 'highest'}"><span class="card-task-priority highest">매우 높음</span></c:when>
                  <c:when test="${task.priority == 'high'}"><span class="card-task-priority high">높음</span></c:when>
                  <c:when test="${task.priority == 'normal'}"><span class="card-task-priority normal">보통</span></c:when>
                  <c:when test="${task.priority == 'low'}"><span class="card-task-priority low">낮음</span></c:when>
                  <c:when test="${task.priority == 'lowest'}"><span class="card-task-priority lowest">매우 낮음</span></c:when>
                </c:choose>
              </div>
            </div>
          </c:forEach>
        </div>
      </div>

      <!-- 우선순위 매우 높은 담당 태스크들 -->
      <div class="kanban-column">
        <div class="column-title">긴급</div>
        <div class="card-list">
          <c:forEach var="task" items="${doneTasks}">
            <div class="card">
              <h3 class="card-title">${task.title}</h3>
              <p class="dueDate">${task.dueDate}</p>
              <div class="card-bottom">
                <span class="card-task-status" style="background-color: ${task.workflowColor}">${task.workflow}</span>
                <c:choose>
                  <c:when test="${task.priority == 'highest'}"><span class="card-task-priority highest">매우 높음</span></c:when>
                  <c:when test="${task.priority == 'high'}"><span class="card-task-priority high">높음</span></c:when>
                  <c:when test="${task.priority == 'normal'}"><span class="card-task-priority normal">보통</span></c:when>
                  <c:when test="${task.priority == 'low'}"><span class="card-task-priority low">낮음</span></c:when>
                  <c:when test="${task.priority == 'lowest'}"><span class="card-task-priority lowest">매우 낮음</span></c:when>
                </c:choose>
              </div>
            </div>
          </c:forEach>
        </div>
      </div>
    </div>
  </div>
</div>