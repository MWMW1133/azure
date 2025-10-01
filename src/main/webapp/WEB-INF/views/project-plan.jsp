<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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
