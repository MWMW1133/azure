<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<html lang="ko">
<head>
  <meta charset="utf-8" />
  <title>회의실</title>

  <!-- 팀에서 쓰는 폰트/부트스트랩 등 필요하면 여기 추가 -->
  <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/variable/pretendardvariable-dynamic-subset.css">
</head>
<body>
  <!-- 회의실이 렌더될 루트 -->
  <main id="room-root"></main>

  <!-- 전용 JS 로드 -->
  <script src="${pageContext.request.contextPath}/js/bootstrap/bootstrap.bundle.js"></script>
  <script src="${pageContext.request.contextPath}/js/sidebar.js?v=spa_2"></script>
  <script src="${pageContext.request.contextPath}/js/notification.js"></script>
  <script src="${pageContext.request.contextPath}/js/todo.js"></script>
  <script src="${pageContext.request.contextPath}/js/chat/modal.js"></script>
  <script src="${pageContext.request.contextPath}/js/meeting.js?v=spa_2"></script>
  <script>
    window.addEventListener('DOMContentLoaded', function () {
      if (window.Meeting) window.Meeting.mount('#room-root');
    });
  </script>
</body>
</html>
