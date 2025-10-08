<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- 회의실이 렌더될 루트 -->
<main id="room-root"></main>

<!-- Agora Web SDK 로드 (UI에는 영향 없음) -->
<script src="https://download.agora.io/sdk/release/AgoraRTC_N.js"></script>

<!-- 서버 값 주입 (하드코딩 금지) -->
<script>
    window.APP = {
        agoraAppId: '${agoraAppId}',
        eventId: '${param.eventId}',
        meetingId: '${param.meetingId}',
        uid: '${sessionScope.userId != null ? sessionScope.userId : "1001"}',
        csrf: '${_csrf.token}'
    };
</script>

<script src="${pageContext.request.contextPath}/js/meeting.js?v=spa_2"></script>
<script>
    window.addEventListener('DOMContentLoaded', function () {
        if (window.Meeting) window.Meeting.mount('#room-root');
    });
</script>
