

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


    <!-- ✅ 외부 컴포넌트 -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css" />
</head>

<body>
<div class="wrapper d-flex">
    <!-- 사이드바 -->
    <jsp:include page="sidebar.jsp"/>

    <main class="main-content">
        <!-- 탑바 -->
        <jsp:include page="topbar.jsp"/>

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
<script src="${pageContext.request.contextPath}/js/my-tasks.js"></script>
<script src="${pageContext.request.contextPath}/js/project-plan.js"></script>
<script src="${pageContext.request.contextPath}/js/projects/mainTable.js"></script>
<script src="${pageContext.request.contextPath}/js/projects/taskRow.js"></script>

<!-- ✅ 6. 채팅방 생성 모달 -->
<jsp:include page="chat/createModal.jsp"/>

<script>
    document.addEventListener("DOMContentLoaded", () => {
        if (typeof connectNotificationSocket === "function" && window.userId) {
            connectNotificationSocket(window.userId);
        }
    });

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

