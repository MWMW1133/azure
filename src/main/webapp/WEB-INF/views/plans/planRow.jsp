<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> <%@ taglib prefix="c" uri="jakarta.tags.core" %> <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<div
  class="pplan-row"
  data-id="${projectPlan.id}"
  data-title="${projectPlan.title}"
  data-proposer="${projectPlan.proposer}"
  data-created-at="${projectPlan.createdAt}"
  data-status="${projectPlan.status}"
  data-start="${projectPlan.startDate}"
  data-end="${projectPlan.endDate}"
  data-description="${fn:escapeXml(projectPlan.description)}"
>
  <div class="pplan-cell pplan-title-cell">${projectPlan.title}</div>
  <div class="pplan-cell pplan-proposer-cell">${projectPlan.proposer}</div>
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
