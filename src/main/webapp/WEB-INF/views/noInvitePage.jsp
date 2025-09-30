<!-- noInvitePage.jsp -->
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>개인유저 로그인</title>
    <!-- Bootstrap CSS -->
    <link href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">

</head>
<body>

<!-- 메인 영역 -->
<div class="main" style="margin-left: 300px;">
    <jsp:include page="topbarTwo.jsp"/>
    <div class="content" style="padding:150px 20px; text-align:center;">
        <h2>프로젝트에 소속되거나 회사의 일원이 되어보세요!</h2>
        <p class="text-muted">아직 참여한 프로젝트가 없습니다.</p>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/bootstrap/bootstrap.bundle.js"></script>
</body>
</html>
