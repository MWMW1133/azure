<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:set var="currentTask" value="${task}" />

<div class="task-row" data-task-id="${currentTask.id}">
    <div class="task-cell task-actions-cell">
        <div class="icon-wrapper">
            <c:if test="${not empty currentTask.subTasks}">
                <span class="toggle-icon js-toggle-subtasks"><i class="fa-solid fa-caret-right"></i></span>
            </c:if>
            <c:if test="${empty currentTask.subTasks}">
                <input type="checkbox" class="form-check-input">
            </c:if>
        </div>
    </div>
    <div class="task-cell task-title-cell">${currentTask.title}</div>
    <div class="task-cell assignee-cell"><img src="/images/${currentTask.assigneeImage}" class="assignee-img" alt="${currentTask.assigneeName}"></div>
    <div class="task-cell started-at-cell">${currentTask.startedAt}</div>
    <div class="task-cell duedate-cell">${currentTask.dueDate}</div>
    <div class="task-cell status-cell"><span class="status ${currentTask.status}">${currentTask.status}</span></div>
    <div class="task-cell priority-cell">
        <c:choose>
            <c:when test="${currentTask.priority == 'highest'}"><span class="priority highest">매우 높음</span></c:when>
            <c:when test="${currentTask.priority == 'high'}"><span class="priority high">높음</span></c:when>
            <c:when test="${currentTask.priority == 'normal'}"><span class="priority normal">보통</span></c:when>
            <c:when test="${currentTask.priority == 'low'}"><span class="priority low">낮음</span></c:when>
            <c:when test="${currentTask.priority == 'lowest'}"><span class="priority lowest">매우 낮음</span></c:when>

        </c:choose>
    </div>
    <div class="task-cell progress-cell">
        <div class="progress-cell-wrapper">
            <span class="progress-value">${currentTask.processPct}%</span>
            <div class="task-progress-container">
                <div class="task-progress-bar" data-progress="${currentTask.processPct}"></div>
            </div>
        </div>
    </div>
    <div class="task-cell file-cell">
        <c:if test="${currentTask.hasFile}"><span class="file-icon"><i class="fa-solid fa-paperclip"></i></span></c:if>
    </div>
    <div class="task-cell updated-at-cell">
        <span class="updated-at-text" style="cursor: pointer;">
            ${currentTask.updatedAt}
        </span>
    </div>
</div>

<%-- 하위 태스크 재귀 호출 --%>
<c:if test="${not empty currentTask.subTasks}">
    <div class="sub-task-container hidden">
        
        <%-- 하위 태스크 --%>
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

        <c:forEach var="subTaskItem" items="${currentTask.subTasks}">
            <c:set var="task" value="${subTaskItem}" scope="request" />
            <jsp:include page="taskRow.jsp" />
        </c:forEach>
    </div>
</c:if>