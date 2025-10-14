<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<c:url value="/api/me"                 var="ME_URL" />
<c:url value="/api/projects"           var="PROJECTS_URL" />
<c:url value="/api/files/presign"      var="PRESIGN_URL" />
<c:url value="/api/meetings/start"     var="START_URL" />
<c:url value="/api/meetings/end"       var="END_URL" />
<c:url value="/api/transcripts/submit" var="SUBMIT_URL" />

<!-- meeting.jsp 어느 위치든 OK -->
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
     data-start-url="${START_URL}"
     data-end-url="${END_URL}"
     data-submit-url="${SUBMIT_URL}"
     style="display:none;">
</div>



<!-- 회의실이 렌더될 루트 -->
<main id="room-root"></main>

<script src="${pageContext.request.contextPath}/js/meeting.js?v=spa_4"></script>

<script>
    window.addEventListener('DOMContentLoaded', function () {
        if (window.Meeting) window.Meeting.mount('#room-root');
    });

    // (선택) 하드코딩이 필요하면 이렇게:
    // function getCurrentProjectId(){ return 1; }
</script>


