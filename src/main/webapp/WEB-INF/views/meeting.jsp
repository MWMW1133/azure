<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

  <!-- 회의실이 렌더될 루트 -->
  <main id="room-root"></main>

  <script src="${pageContext.request.contextPath}/js/meeting.js?v=spa_2"></script>
  <script>
    window.addEventListener('DOMContentLoaded', function () {
      if (window.Meeting) window.Meeting.mount('#room-root');
    });
  </script>

