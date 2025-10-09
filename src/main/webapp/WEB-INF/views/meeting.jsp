<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- 회의실이 렌더될 루트 (기존 그대로) -->
<main id="room-root"></main>

<!-- Agora Web SDK (클릭 전까지 로딩만; UI 영향 없음) -->
<script src="https://download.agora.io/sdk/release/AgoraRTC_N.js"></script>

<!-- 너희 meeting.js (스타일/마크업 포함된 기존 파일) -->
<script src="${pageContext.request.contextPath}/js/meeting.js?v=spa_2"></script>

<script>
    // 기존 mount 흐름 유지
    window.addEventListener('DOMContentLoaded', function () {
        if (window.Meeting) window.Meeting.mount('#room-root');
    });

    // 기존 셀렉트 연동 함수(그대로)
    function getCurrentProjectId() {
        const sel = document.querySelector('#projectSelect');
        return sel ? Number(sel.value) : null;
    }
</script>
