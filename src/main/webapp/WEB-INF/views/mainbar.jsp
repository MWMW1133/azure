<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="utf-8" />
    <title>Mainbar</title>

    <!-- Bootstrap & Icons -->
    <link href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">

    <!-- 각자 css 불러오기(나중에 파일 분리하면) -->
    <!-- <link href="${pageContext.request.contextPath}/css/sidebar.css" rel="stylesheet"> -->
    <!-- <link href="${pageContext.request.contextPath}/css/topbar.css" rel="stylesheet"> -->
</head>
<body>
<div class="app">
    <jsp:include page="sidebar.jsp"/>
    <main class="main-content">
        <jsp:include page="topbar.jsp"/>
        <div class="page-body">
        </div>
    </main>
</div>

<!-- JS -->
<script src="${pageContext.request.contextPath}/js/bootstrap/bootstrap.bundle.js"></script>
<script src="${pageContext.request.contextPath}/js/sidebar.js"></script>
<script src="${pageContext.request.contextPath}/js/notification.js"></script>
<script src="${pageContext.request.contextPath}/js/todo.js"></script>
<script src="${pageContext.request.contextPath}/js/chat/modal.js"></script>
<script src="${pageContext.request.contextPath}/js/meeting.js"></script>
</body>
</html>
