
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="utf-8" />
    <title>Azura Workspace</title>

    <!-- ✅ 1. 공통 라이브러리 CSS -->
    <link href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">

    <!-- Pretendard 폰트 (전역 적용) -->
    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/variable/pretendardvariable-dynamic-subset.css">

    <!-- ✅ 2. 프로젝트 공통 CSS -->
    <link href="${pageContext.request.contextPath}/css/layout/layout.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/layout/sidebar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/layout/topbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/chat.css" rel="stylesheet">

    <!-- ✅ 3. 각 기능별 CSS (캘린더, 태스크 등) -->
    <link href="${pageContext.request.contextPath}/css/my-calendar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/my-tasks.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/taskRow.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/project-plan.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/home.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/projects/mainTable.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/projects/taskForm.css" rel="stylesheet">

    <link href="${pageContext.request.contextPath}/css/files.css" rel="stylesheet">

    <!-- ✅ FullCalendar 필수 CSS (CDN + 로컬 폴백) -->
    <link rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.15/index.global.min.css">
    <script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.15/index.global.min.js" defer></script>

    <!-- ✅ 프로젝트 캘린더 전용 CSS -->
    <link href="${pageContext.request.contextPath}/css/projects/project-calendar.css" rel="stylesheet">

    <!-- ✅ 외부 컴포넌트 -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css" />

    <link rel="icon" href="${pageContext.request.contextPath}/favicon.ico" />
</head>

<body>
  <div class="wrapper">
    <!-- 사이드바 -->
    <jsp:include page="sidebar.jsp"/>
    <!-- 본문 -->
    <main class="main-content">
        <!-- 탑바 -->
        <jsp:include page="/WEB-INF/views/topbar.jsp"/>

        <!-- Body -->
        <div class="page-body">
            <c:import url="${body}" />
        </div>
    </main>
</div>


<!-- ✅ 4. 공통 JS 라이브러리 -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7.0.0/bundles/stomp.umd.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.14/index.global.min.js"></script>
<!-- ✅ 프로젝트 캘린더 전용 JS (전역 로드) -->
<script defer src="${pageContext.request.contextPath}/js/projects/project-calendar.js"></script>


<!-- ✅ 5. 프로젝트 공통 JS -->
<script src="${pageContext.request.contextPath}/js/sidebar.js"></script>
<script src="${pageContext.request.contextPath}/js/notification.js"></script>
<script src="${pageContext.request.contextPath}/js/todo.js"></script>
<script src="${pageContext.request.contextPath}/js/chat/modal.js"></script>
<script src="${pageContext.request.contextPath}/js/search.js"></script>
<script src="${pageContext.request.contextPath}/js/profile.js"></script>
<script src="${pageContext.request.contextPath}/js/inviteColleagues.js"></script>

<script src="${pageContext.request.contextPath}/js/meeting.js"></script>

<script src="${pageContext.request.contextPath}/js/my-calendar.js"></script>
<script src="${pageContext.request.contextPath}/js/my-tasks.js" defer></script>
<script src="${pageContext.request.contextPath}/js/project-plan.js"></script>
<script src="${pageContext.request.contextPath}/js/projects/mainTable.js"></script>
<script src="${pageContext.request.contextPath}/js/projects/taskRow.js"></script>

  <script src="${pageContext.request.contextPath}/js/chat/client.js"></script>
<script src="${pageContext.request.contextPath}/js/files.js"></script>


<!-- ✅ 6. 채팅방 생성 모달 -->
<jsp:include page="chat/createModal.jsp"/>

<script>
    document.addEventListener("DOMContentLoaded", () => {
        let openMenu = null;

        const forceCloseAll = () => {
            document.querySelectorAll(".dropdown-menu.show").forEach(m => m.classList.remove("show"));
            openMenu = null;
        };

        // 트리거(벨/투두/프로필) 기준으로 메뉴 좌표 계산
        const positionMenu = (trigger, menu) => {
            const r = trigger.getBoundingClientRect();
            const top  = r.bottom + 8;
            const right = window.innerWidth - r.right;
            menu.style.position = "fixed";
            menu.style.left = "auto";
            menu.style.right = `${right}px`;
            menu.style.top = `${top}px`;
            menu.style.transform = "none";
            menu.style.zIndex = "10000";
        };

        const bindDropdowns = () => {
            forceCloseAll();

            document.querySelectorAll("[data-bs-toggle='dropdown']").forEach(trigger => {
                if (trigger._azuraBound) return;
                trigger._azuraBound = true;

                // Bootstrap 기본 dropdown 기능 완전히 비활성화
                trigger.removeAttribute("data-bs-toggle");

                const menu = trigger.nextElementSibling;
                if (!menu) return;

                //  Bootstrap 내부 이벤트 차단
                ["hide.bs.dropdown","hidden.bs.dropdown","show.bs.dropdown","shown.bs.dropdown"]
                    .forEach(ev => menu.addEventListener(ev, e => e.preventDefault()));

                // 클릭 시 열기/닫기 토글
                const onClick = (e) => {
                    e.preventDefault();
                    e.stopPropagation();

                    const willOpen = !menu.classList.contains("show");

                    // 다른 메뉴 닫기
                    if (openMenu && openMenu !== menu) openMenu.classList.remove("show");

                    if (willOpen) {
                        positionMenu(trigger, menu);
                        menu.classList.add("show");
                        openMenu = menu;
                        console.log("[Azura] OPEN:", trigger.id);
                    } else {
                        menu.classList.remove("show");
                        openMenu = null;
                        console.log("[Azura] CLOSE:", trigger.id);
                    }
                };

                // 실제 연결 (onDown → onClick 교체)
                trigger.addEventListener("click", onClick);
            });
        };

        // 문서 아무 곳 클릭 시 닫기
        document.addEventListener("click", (e) => {
            if (!openMenu) return;
            setTimeout(() => {
                const isInside = e.target.closest(".dropdown-menu") || e.target.closest("[data-bs-toggle='dropdown']");
                if (!isInside) {
                    openMenu.classList.remove("show");
                    openMenu = null;
                    console.log("[Azura] CLOSE: outside");
                }
            }, 50);
        });

        // 초기 1회 + 프래그먼트 교체 때마다
        bindDropdowns();
        const obs = new MutationObserver((muts) => {
            for (const m of muts) {
                if (m.addedNodes.length) { bindDropdowns(); break; }
            }
        });
        obs.observe(document.querySelector(".main-content") || document.body, { childList: true, subtree: true });
        window.addEventListener("beforeunload", () => obs.disconnect());
    });
</script>
<!-- 삭제하지마세요 유저아이디 세팅중입니다 -->
<c:if test="${not empty user}">
    <script>
        window.USER_ID = ${user.id};
        window.APP_CONTEXT = '${pageContext.request.contextPath}';
    </script>
</c:if>
  <!-- 전역 컨텍스트 & 현재 로그인 유저 id 노출 (JSP 안전 주입) -->
  <script>
    (function (w) {
      // 컨텍스트 경로는 문자열이므로 따옴표로 감싸서 주입
      w.APP_CTX = '<c:out value="${pageContext.request.contextPath}"/>';

      // 유저 ID는 숫자로 주입: 비로그인/널이면 0
      w.CURRENT_USER_ID = <c:out value="${empty user or empty user.id ? 0 : user.id}"/>;

      // 앱 플래그 유지
      w.APP = w.APP || {};
      w.APP.useBackend = true;
    })(window);
  </script>

  <!-- =========================================================
       전역 초기화 (앱 스크립트 로드 후)
     ========================================================= -->
  <script>
    document.addEventListener("DOMContentLoaded", () => {
      // 알림 소켓 연결 (유저 존재 시)
      if (window.CURRENT_USER_ID > 0 && typeof window.connectNotificationSocket === 'function') {
        window.connectNotificationSocket(window.CURRENT_USER_ID);
      } else {
        console.warn('connectNotificationSocket 미로딩 또는 사용자 ID 없음');
      }

      // 사이드바 바깥으로 튀는 회의실 링크 제거(기존 로직 유지)
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