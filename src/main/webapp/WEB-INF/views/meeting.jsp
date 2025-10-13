<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!-- CSRF (Spring Security 사용 시) -->
<sec:csrfMetaTags />

<!-- 회의실이 렌더될 루트 -->
<main id="room-root"></main>

<script src="${pageContext.request.contextPath}/js/meeting.js?v=spa_3"></script>
<script>
    window.addEventListener('DOMContentLoaded', function () {
        if (window.Meeting) window.Meeting.mount('#room-root');
    });

    // (선택) 하드코딩이 필요하면 이렇게:
    // function getCurrentProjectId(){ return 1; }
</script>
