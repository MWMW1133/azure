<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> <%@ taglib prefix="c" uri="jakarta.tags.core" %> <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<div
  class="pplan-row"
  data-id="${projectPlan.id}"
  data-title="${projectPlan.title}"
  data-proposer="${projectPlan.proposer}"
  data-proposer-avatarUrl="${projectPlan.proposerAvatarUrl}"
  data-created-at="${projectPlan.createdAt}"
  data-status="${projectPlan.status}"
  data-start="${projectPlan.startDate}"
  data-end="${projectPlan.endDate}"
  data-description="${fn:escapeXml(projectPlan.description)}"
>
  <div class="pplan-cell pplan-title-cell">${projectPlan.title}</div>
  <div class="pplan-cell pplan-proposer-cell">
    <div class="avatar" tabindex="0" aria-label="${projectPlan.proposer}">
      <c:choose>
        <c:when test="${not empty projectPlan.proposerAvatarUrl}">
          <img src="${projectPlan.proposerAvatarUrl}" alt="${projectPlan.proposer}" title="${projectPlan.proposer}" />
        </c:when>
        <c:otherwise>
          <!-- 아바타가 없으면 이니셜 한 글자 -->
          <span class="initials">
            <c:out value="${fn:substring(projectPlan.proposer, 0, 1)}"/>
          </span>
        </c:otherwise>
      </c:choose>
      <span class="tooltip">${projectPlan.proposer}</span>
    </div>
  </div>
  <div class="pplan-cell pplan-created-at-cell">${projectPlan.createdAt}</div>
  <div class="pplan-cell pplan-status-cell">
    <c:choose>
      <c:when test="${projectPlan.status eq 'new'}">
        <span class="pplan-status new">검토 전</span>
      </c:when>
      <c:when test="${projectPlan.status eq 'approved'}">
        <span class="pplan-status approved">승인됨</span>
      </c:when>
      <c:when test="${projectPlan.status eq 'rejected'}">
        <span class="pplan-status rejected">거부됨</span>
      </c:when>
    </c:choose>
  </div>
  <div class="pplan-cell pplan-description-cell">${projectPlan.description}</div>
  <div class="pplan-cell pplan-duration-cell">${projectPlan.startDate} ~ ${projectPlan.endDate}</div>
</div>
