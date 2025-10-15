<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<div class="management-wrapper"
     data-ctx="${pageContext.request.contextPath}"
     data-project-id="${projectId}">
  <!-- 멤버 관리 -->
  <div class="management-card management-member-wrapper">
    <div class="management-card-header">
      <span class="management-member-title">멤버 관리</span>
    </div>
    <div class="management-member-body">
      <c:forEach var="member" items="${memberList}">
        <c:set var="avatarUrl" value="${member.avatarUrl}" />
        <c:set var="memberName" value="${member.name}" />
        <div class="member-row" data-member-id="${member.id}">
          <c:choose>
            <c:when test="${not empty avatarUrl}">
              <img src="${avatarUrl}" alt="${fn:escapeXml(memberName)}" class="member-img" />
            </c:when>
            <c:otherwise>
              <div class="member-initial">
                <c:out value="${fn:length(memberName) == 0 ? '?' : fn:substring(memberName, 0, 1)}" />
              </div>
            </c:otherwise>
          </c:choose>

          <div class="member-name">
            <c:out value="${memberName}" />
          </div>

          <button class="management-member-action" data-member-id="${member.id}" aria-label="${memberName} 삭제">삭제</button>
        </div>
      </c:forEach>

      <c:if test="${empty memberList}">
        <div class="empty-hint">아직 멤버가 없습니다. 멤버를 초대해 보세요.</div>
      </c:if>
    </div>
  </div>

  <!-- 프로젝트 관리  -->
  <div class="management-card management-project-wrapper">
    <div class="management-card-header">
      <span class="management-project-title">프로젝트 관리</span>
    </div>
    <div class="management-project-body">
      <%-- 프로젝트 정보 같은걸 더 넣어야할까.................?--%>
      <div class="danger-zone">
        <button class="management-project-delete" data-project-id="${project.id}" aria-label="프로젝트 삭제">프로젝트 삭제</button>
      </div>
    </div>
  </div>
</div>
