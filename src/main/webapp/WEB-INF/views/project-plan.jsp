<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> 
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!-- 서버 값 주입 -->
<div id="pplan-root"
     data-current-user-id="${currentUserId}"
     data-organization-id="${organizationId}">
</div>

<div class="pplan-container">
  <div class="pplan-container-top">
    <div class="pplan-title">프로젝트 계획</div>
    <button class="pplan-add-button" id="btn-add">계획 추가하기</button>
  </div>

  <div class="pplan-container-body">
    <div class="new-pplan-container">
      <div class="pplan-container-title new-pplan-title">새로운 계획</div>
      <jsp:include page="/WEB-INF/views/plans/planHeader.jsp" />
      <div class="pplan-list-body">
        <c:forEach var="planItem" items="${newPlans}">
          <c:set var="projectPlan" value="${planItem}" scope="request" />
          <jsp:include page="/WEB-INF/views/plans/planRow.jsp" />
        </c:forEach>
      </div>
    </div>

    <div class="approved-pplan-container">
      <div class="pplan-container-title approved-pplan-title">승인된 계획</div>
      <jsp:include page="/WEB-INF/views/plans/planHeader.jsp" />
      <div class="pplan-list-body">
        <c:forEach var="planItem" items="${approvedPlans}">
          <c:set var="projectPlan" value="${planItem}" scope="request" />
          <jsp:include page="/WEB-INF/views/plans/planRow.jsp" />
        </c:forEach>
      </div>
    </div>

    <div class="rejected-pplan-container">
      <div class="pplan-container-title rejected-pplan-title">거부된 계획</div>
      <jsp:include page="/WEB-INF/views/plans/planHeader.jsp" />
      <div class="pplan-list-body">
        <c:forEach var="planItem" items="${rejectedPlans}">
          <c:set var="projectPlan" value="${planItem}" scope="request" />
          <jsp:include page="/WEB-INF/views/plans/planRow.jsp" />
        </c:forEach>
      </div>
    </div>
  </div>
</div>

<jsp:include page="/WEB-INF/views/plans/planForm.jsp" />
<jsp:include page="/WEB-INF/views/plans/planViewer.jsp" />

<!-- Toast -->
<div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 2000">
  <div id="planToast" class="toast clean-toast" role="alert" aria-live="assertive" aria-atomic="true">
    <div class="d-flex align-items-center">
      <div class="toast-icon me-2" aria-hidden="true"></div>
      <div class="toast-body">메시지</div>
    </div>
  </div>
</div>

<!-- 프로젝트 계획 전용 JS -->
<script src="${pageContext.request.contextPath}/js/project-plan.js"></script>
<script>
  document.addEventListener("DOMContentLoaded", () => {
    if (window.ProjectPlan?.mount) {
      window.ProjectPlan.mount(document);
    } else {
      console.error("[project-plan] ProjectPlan.js가 로드되지 않았습니다.");
    }
  });
</script>
