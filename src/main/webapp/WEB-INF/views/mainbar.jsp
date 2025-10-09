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

  <!-- ===== App CSS (layout.css는 항상 마지막) ===== -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/topbar.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/sidebar.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/chat.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/my-calendar.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/my-tasks.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/taskRow.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/project-plan.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css" />

  <link rel="icon" href="${pageContext.request.contextPath}/favicon.ico" />
</head>
<body>
  <div class="wrapper">
    <!-- 사이드바 -->
    <jsp:include page="sidebar.jsp"/>
    <!-- 본문 -->
    <main class="main-content">
      <!-- 탑바 (마크업만 있어야 함) -->
      <jsp:include page="topbar.jsp"/>
      <div class="page-body">
        <jsp:include page="${body}" />
      </div>
    </main>
  </div>

  <!-- 모달 포함 위치는 기존 그대로 유지 -->
  <jsp:include page="chat/createModal.jsp"/>

  <!-- =========================================================
       [전역 주입 - 스크립트 로딩 전]  ✅ 반드시 앱 스크립트보다 먼저!
       - 컨텍스트 경로, 유저/권한, 채널맵(임시), 백엔드 모드 플래그
       - 여기서 값을 세팅해두면 modal.js / client.js에서 바로 사용 가능
     ========================================================= -->
  <script>
    // 컨텍스트 경로
    window.APP_CTX = '${pageContext.request.contextPath}';

    // 유저/권한 (비어있으면 기본값)
    window.CURRENT_USER_ID = ${empty user or empty user.id ? 0 : user.id};
    window.USER_ROLE = '<c:out value="${org.role}" default=""/>';
    
    // 채팅 채널 매핑(임시) + 백엔드 모드
    window.APP = window.APP || {};
    // TODO: 실제 DB 값으로 교체 (현재는 테스트용)
    window.APP.channelMap = { '프로젝트 1': 1, '프로젝트 2': 2, '프로젝트 3': 3 
                              '홍길동': 101, '김철수': 102, '이영희': 103 };
    window.APP.useBackend = true;
  </script>

  <!-- =========================================================
       [라이브러리] (부트스트랩/플랫피커/풀캘린더 + 소켓)
     ========================================================= -->
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
  <script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.14/index.global.min.js"></script>

  <!-- WebSocket libs (SockJS + stomp 레거시; notification.js와 호환) -->
  <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
  <!-- ✅ client.js가 사용하는 신형 STOMP (전역명: StompJs) -->
  <script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7.0.0/bundles/stomp.umd.min.js"></script>

  <!-- =========================================================
       [앱 스크립트]  ← 위 전역 주입/라이브러리 이후에 로드
     ========================================================= -->
  <script src="${pageContext.request.contextPath}/js/sidebar.js"></script>
  <script src="${pageContext.request.contextPath}/js/notification.js"></script>
  <script src="${pageContext.request.contextPath}/js/todo.js"></script>

  <!-- 채팅 (모달 UI → WS/REST 클라이언트) -->
  <script src="${pageContext.request.contextPath}/js/chat/modal.js"></script>
  <script src="${pageContext.request.contextPath}/js/chat/client.js"></script>

  <script src="${pageContext.request.contextPath}/js/my-tasks.js"></script>
  <script src="${pageContext.request.contextPath}/js/project-plan.js"></script>
  <script src="${pageContext.request.contextPath}/js/search.js"></script>
  <script src="${pageContext.request.contextPath}/js/meeting.js"></script>
  <script src="${pageContext.request.contextPath}/js/profile.js"></script>

  <!-- =========================================================
       [전역 초기화]  ← 앱 스크립트 로드 후에 한 번만 실행
       - 알림 소켓 연결
       - 사이드바 바깥으로 튀는 회의실 링크 제거(기존 로직 유지)
     ========================================================= -->
  <script>
    document.addEventListener("DOMContentLoaded", () => {
      // 알림 소켓 연결 (유저가 있을 때만)
      if (window.CURRENT_USER_ID > 0 && typeof window.connectNotificationSocket === 'function') {
        window.connectNotificationSocket(window.CURRENT_USER_ID);
      } else {
        console.warn('connectNotificationSocket 미로딩 또는 사용자 ID 없음');
      }

      // 사이드바 바깥으로 잘못 튀는 회의실 링크 제거(필요 시 유지)
      (function () {
        function purgeOrphans() {
          document.querySelectorAll('a.proj-row.room').forEach(function (n) {
            if (!n.closest('.sidebar')) n.remove();
          });
        }
        purgeOrphans();
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
    });
  </script>
</body>
</html>
