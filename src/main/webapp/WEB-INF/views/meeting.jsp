<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!-- Spring Environment에서 application.properties 값 가져오기 -->
<spring:eval expression="@environment.getProperty('app.public-base-url')" var="AWS_S3_PUBLIC_BASE_URL"/>
<spring:eval expression="@environment.getProperty('agora.app-id')"       var="AGORA_APP_ID"/>

<!-- Agora SDK 먼저 로드 -->
<script src="https://download.agora.io/sdk/release/AgoraRTC_N.js"></script>
<!-- 앱 스크립트 -->
<script src="${pageContext.request.contextPath}/js/meeting.js?v=spa_4"></script>

<!-- API 엔드포인트들 -->
<c:url value="/api/me"                 var="ME_URL" />
<c:url value="/api/projects"           var="PROJECTS_URL" />
<c:url value="/api/files/presign"      var="PRESIGN_URL" />
<c:url value="/api/meetings/start"     var="START_URL" />
<c:url value="/api/meetings"           var="END_URL" />
<c:url value="/api/transcripts/submit" var="SUBMIT_URL" />

<!-- 프런트에서 읽을 설정 블록 -->
<div id="meeting-config"
     data-me-url="${ME_URL}"
     data-projects-url="${PROJECTS_URL}"

     data-project-members-url="/api/projects/{id}/members"
     data-project-invitations-url="/api/projects/{id}/invitations"
     data-project-invite-url="/api/projects/{id}/invitations"
     data-project-accept-url="/api/projects/{id}/members"
     data-project-decline-url="/api/projects/{id}/invitations/{userId}"
     data-project-remove-url="/api/projects/{id}/members/{userId}"


     data-presign-url="${PRESIGN_URL}"
     data-public-base-url="${AWS_S3_PUBLIC_BASE_URL}"
     data-agora-app-id ="${AGORA_APP_ID}"
     data-start-url="${START_URL}"
     data-end-url="${END_URL}"
     data-submit-url="${SUBMIT_URL}"
     data-transcript-latest-url="/api/transcripts/{meetingId}/latest"
     style="display:none;">
</div>

<!-- 회의실 렌더 타깃 -->
<main id="room-root"></main>

<script>
  window.addEventListener('DOMContentLoaded', function () {
    if (window.Meeting) window.Meeting.mount('#room-root');
  });
</script>