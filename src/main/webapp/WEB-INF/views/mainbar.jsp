<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="utf-8" />
    <title>Mainbar</title>

    <!-- Bootstrap & Icons -->
    <link href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.css" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" />
    <link href="${pageContext.request.contextPath}/css/my-calendar.css" rel="stylesheet" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/my-tasks.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/taskRow.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/project-plan.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css" />

    <!-- 각자 css 불러오기(나중에 파일 분리하면) -->
    <!-- <link href="${pageContext.request.contextPath}/css/sidebar.css" rel="stylesheet"> -->
    <!-- <link href="${pageContext.request.contextPath}/css/topbar.css" rel="stylesheet"> -->
  </head>
  <body>
    <div class="app">
      <jsp:include page="sidebar.jsp" />
      <main class="main-content">
        <jsp:include page="topbar.jsp" />
        <div class="page-body" style="height: 815px"></div>
      </main>
    </div>

    <!-- JS -->
    <script src="${pageContext.request.contextPath}/js/sidebar.js"></script>
    <script src="${pageContext.request.contextPath}/js/notification.js"></script>
    <script src="${pageContext.request.contextPath}/js/todo.js"></script>
    <script src="${pageContext.request.contextPath}/js/chat/modal.js"></script>
    <script src="${pageContext.request.contextPath}/js/my-calendar.js"></script>
    <script src="${pageContext.request.contextPath}/js/my-tasks.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
    <script src="${pageContext.request.contextPath}/js/project-plan.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.14/index.global.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/@fullcalendar/core@6.1.15/index.global.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/@fullcalendar/daygrid@6.1.15/index.global.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/@fullcalendar/timegrid@6.1.15/index.global.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/@fullcalendar/interaction@6.1.15/index.global.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/@fullcalendar/rrule@6.1.15/index.global.min.js"></script>
    <!-- ★ -->
  </body>
</html>
