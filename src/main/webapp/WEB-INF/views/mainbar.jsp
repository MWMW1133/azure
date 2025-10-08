<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<html lang="ko">
<head>
  <meta charset="utf-8" />
  <title>Mainbar</title>

  <!-- ===== Fonts (전역) ===== -->
  <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/variable/pretendardvariable-dynamic-subset.css" />

  <!-- ===== Vendor CSS ===== -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.css" />
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" />
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" />
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css" />

  <!-- ===== App CSS ===== -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/topbar.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/sidebar.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/chat.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/my-calendar.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/my-tasks.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/taskRow.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/project-plan.css" />

  <link rel="icon" href="${pageContext.request.contextPath}/favicon.ico" />
</head>
<body>
  <!-- 전역 컨텍스트 (경로 계산용) -->
  <script>window.APP_CTX='${pageContext.request.contextPath}';</script>

  <div class="wrapper d-flex">
    <!-- 사이드바 -->
    <jsp:include page="sidebar.jsp"/>
    <!-- 본문 -->
    <main class="main-content">
      <!-- 탑바 -->
      <jsp:include page="topbar.jsp"/>
      <div class="page-body">
        <jsp:include page="${body}" />
      </div>
    </main>

<<<<<<< HEAD
    <!-- 모달 템플릿(마크업만; JS는 아래에서 공통 로드) -->
=======

    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>

    <!-- JS -->
    <script src="${pageContext.request.contextPath}/js/sidebar.js"></script>
    <script src="${pageContext.request.contextPath}/js/notification.js"></script>
    <script src="${pageContext.request.contextPath}/js/todo.js"></script>
    <script src="${pageContext.request.contextPath}/js/chat/modal.js"></script>
    <script src="${pageContext.request.contextPath}/js/my-tasks.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
    <script src="${pageContext.request.contextPath}/js/project-plan.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>



>>>>>>> origin/sohyun
    <jsp:include page="chat/createModal.jsp"/>
  </div>

  <!-- ===== JS (라이브러리 → 앱 스크립트 순서, 각 1회) ===== -->
  <!-- 라이브러리 -->
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
  <script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.14/index.global.min.js"></script>

<<<<<<< HEAD
  <!-- 앱 스크립트 -->
  <script src="${pageContext.request.contextPath}/js/sidebar.js"></script>
  <script src="${pageContext.request.contextPath}/js/notification.js"></script>
  <script src="${pageContext.request.contextPath}/js/todo.js"></script>
  <script src="${pageContext.request.contextPath}/js/chat/modal.js"></script>
  <script src="${pageContext.request.contextPath}/js/my-tasks.js"></script>
  <script src="${pageContext.request.contextPath}/js/project-plan.js"></script>
  <script src="${pageContext.request.contextPath}/js/my-calendar.js"></script>
  <script src="${pageContext.request.contextPath}/js/meeting.js"></script>

  <!-- 🔧 최종 안전장치: 사이드바 바깥의 잘못된 회의실 링크를 감시/즉시 제거 -->
  <script>
    (function () {
      function purgeOrphans() {
        document.querySelectorAll('a.proj-row.room').forEach(function (n) {
          if (!n.closest('.sidebar')) n.remove(); // 사이드바 밖이면 제거
        });
      }

      // 초기 1회 정리
      purgeOrphans();

      // 이후 동적 삽입까지 감시해서 즉시 제거
      var obs = new MutationObserver(function (mutations) {
        for (var i = 0; i < mutations.length; i++) {
          if (mutations[i].addedNodes && mutations[i].addedNodes.length) {
            purgeOrphans();
            break;
          }
        }
      });
      obs.observe(document.body, { childList: true, subtree: true });
      window.addEventListener('beforeunload', function(){ obs.disconnect(); });
    })();
  </script>
</body>
</html>
=======
    <script src="${pageContext.request.contextPath}/js/bootstrap/bootstrap.bundle.js"></script>
    <script src="${pageContext.request.contextPath}/js/sidebar.js"></script>
    <script src="${pageContext.request.contextPath}/js/notification.js"></script>
    <script src="${pageContext.request.contextPath}/js/todo.js"></script>
    <script src="${pageContext.request.contextPath}/js/chat/modal.js"></script>
    <script src="${pageContext.request.contextPath}/js/search.js"></script>
    <script src="${pageContext.request.contextPath}/js/meeting.js"></script>

  </body>
</html>
>>>>>>> origin/sohyun
