<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> 
<%@ taglib prefix="c" uri="jakarta.tags.core" %> 
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<div
  class="pplan-row"
  data-id="${projectPlan.id}"
  data-title="${projectPlan.name}"                
  data-proposer="${projectPlan.proposerName}"      
  data-proposer-avatarUrl="${projectPlan.proposerAvatarUrl}"
  data-created-at="${projectPlan.createdAt}"
  data-status="${projectPlan.status}"
  data-start="${projectPlan.startDate}"
  data-end="${projectPlan.dueDate}"                
  data-description="${fn:escapeXml(projectPlan.description)}"
>
  <!-- 프로젝트 명 -->
  <div class="pplan-cell pplan-title-cell">${projectPlan.name}</div>

  <!-- 작성자 -->
  <div class="pplan-cell pplan-proposer-cell">
    <div class="avatar" tabindex="0" aria-label="${projectPlan.proposerName}">
      <c:choose>
        <c:when test="${not empty projectPlan.proposerAvatarUrl}">
          <img src="${projectPlan.proposerAvatarUrl}" 
               alt="${projectPlan.proposerName}" 
               title="${projectPlan.proposerName}" />
        </c:when>
        <c:otherwise>
          <!-- 아바타 없으면 이니셜 한 글자 -->
          <span class="initials">
            <c:out value="${fn:substring(projectPlan.proposerName, 0, 1)}"/>
          </span>
        </c:otherwise>
      </c:choose>
      <span class="tooltip">${projectPlan.proposerName}</span>
    </div>
  </div>

  <!-- 작성일 -->
  <div class="pplan-cell pplan-created-at-cell">
  <c:choose>
    <c:when test="${not empty projectPlan.createdAt}">
      ${fn:substring(projectPlan.createdAt, 0, 10)}
    </c:when>
    <c:otherwise>-</c:otherwise>
  </c:choose>
</div>

  <!-- 상태 -->
  <div class="pplan-cell pplan-status-cell">
    <c:choose>
      <c:when test="${projectPlan.status eq 'PENDING'}">
        <span class="pplan-status new">검토 전</span>
      </c:when>
      <c:when test="${projectPlan.status eq 'APPROVED'}">
        <span class="pplan-status approved">승인됨</span>
      </c:when>
      <c:when test="${projectPlan.status eq 'REJECTED'}">
        <span class="pplan-status rejected">거부됨</span>
      </c:when>
    </c:choose>
  </div>

  <!-- 설명 -->
  <div class="pplan-cell pplan-description-cell">${projectPlan.description}</div>

  <!-- 기간 -->
  <div class="pplan-cell pplan-duration-cell">${projectPlan.startDate} ~ ${projectPlan.dueDate}</div>
</div>