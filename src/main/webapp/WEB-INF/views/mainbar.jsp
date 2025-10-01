
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<html lang="ko">
<head>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="utf-8" />
    <title>Mainbar</title>

    <!-- Bootstrap & Icons -->
    <link href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">

    <!-- 각자 css 불러오기(나중에 파일 분리하면) -->

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/chat.css">
    <link href="${pageContext.request.contextPath}/css/layout.css" rel="stylesheet">

    <!-- <link href="${pageContext.request.contextPath}/css/sidebar.css" rel="stylesheet"> -->
    <!-- <link href="${pageContext.request.contextPath}/css/topbar.css" rel="stylesheet"> -->
</head>
<body>
<div class="wrapper d-flex">
    <!-- 사이드바 -->
    <jsp:include page="sidebar.jsp"/>

    <main class="main-content">

        <!-- 탑바 -->
<%--        <div style="background:cyan; padding:5px;">[DEBUG] topbar include 시작</div>--%>
        <jsp:include page="topbar.jsp"/>
<%--        <div style="background:cyan; padding:5px;"> body 속성 = <c:out value="${body}" default="(없음)" /></div>--%>

        <!-- body -->
        <div class="page-body">
<%--            <div style="background:pink; padding:5px;">BODY 값 = <c:out value="${body}" /></div>--%>
<%--            <jsp:include page="${body}" />--%>
<%--            <c:import url="/WEB-INF/views/${body}"/>--%>
            <c:import url="${body}" />
<%--            <div style="background:pink; padding:5px;">[DEBUG] body include 끝</div>--%>
        </div>
    </main>


<!-- sb -->
<%--  <body>--%>
<%--    <div class="app">--%>
<%--      <jsp:include page="sidebar.jsp" />--%>
<%--      <main class="main-content">--%>
<%--        <jsp:include page="topbar.jsp" />--%>
<%--        <div class="page-body" style="height: 815px"></div>--%>
<%--      </main>--%>
<%--    </div>--%>

    <!-- JS -->
    <script src="${pageContext.request.contextPath}/js/sidebar.js"></script>
    <script src="${pageContext.request.contextPath}/js/notification.js"></script>
    <script src="${pageContext.request.contextPath}/js/todo.js"></script>
    <script src="${pageContext.request.contextPath}/js/chat/modal.js"></script>
    <script src="${pageContext.request.contextPath}/js/my-tasks.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
    <script src="${pageContext.request.contextPath}/js/project-plan.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>


    <script src="${pageContext.request.contextPath}/js/bootstrap/bootstrap.bundle.js"></script>
    <script src="${pageContext.request.contextPath}/js/sidebar.js"></script>
    <script src="${pageContext.request.contextPath}/js/notification.js"></script>
    <script src="${pageContext.request.contextPath}/js/todo.js"></script>
    <script src="${pageContext.request.contextPath}/js/chat/modal.js"></script>
    <script src="${pageContext.request.contextPath}/js/search.js"></script>
    <script src="${pageContext.request.contextPath}/js/meeting.js"></script>
    <%--<jsp:include page="/WEB-INF/views/chat/createModal.jsp"/>--%>
    <jsp:include page="chat/createModal.jsp"/>
    <!-- ★ -->

    <link href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.css" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" />
    <link href="${pageContext.request.contextPath}/css/my-calendar.css" rel="stylesheet" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/my-tasks.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/taskRow.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/project-plan.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css" />
    <script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.14/index.global.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/my-calendar.js"></script>

  </body>
</html>