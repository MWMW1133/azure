<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

  <!-- 회의실이 렌더될 루트 -->
  <main id="room-root"></main>

  <script src="${pageContext.request.contextPath}/js/meeting.js?v=spa_2"></script>
  <script>
    window.addEventListener('DOMContentLoaded', function () {
      if (window.Meeting) window.Meeting.mount('#room-root');
    });
    //
    // function getCurrentProjectId() {
    //   return 1; // 하드코딩
    // }

    // 실제 드롭다운과 연동 시 이런식으로 변경
    function getCurrentProjectId() {
      const sel = document.querySelector('#projectSelect');
      return sel ? Number(sel.value) : null;
    }
  </script>

