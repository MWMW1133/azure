<%@ page contentType="text/html;charset=UTF-8" %>
    <!-- Bootstrap CSS -->
    <link href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.css" rel="stylesheet">

    <style>
        body {
            margin: 0;
            font-family: Arial, sans-serif;
        }
        .main { margin-left: 0 !important; }
        .topbar {
            position: sticky;
            top: 0;
            z-index: 1000;
            height: 60px;
            background: #fff;
            border-bottom: 1px solid #ddd;
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 20px;
        }
        .topbar .left {
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .topbar .logo {
            height: 28px;
            width: auto;
        }
        .topbar .right {
            display: flex;
            align-items: center;
            gap: 18px;
        }
    </style>
</head>
<body>

<!-- 메인 영역 -->
<div class="main">
    <!-- 탑바 -->
    <div class="topbar">
        <div class="left">
            <img src="${pageContext.request.contextPath}/images/logo/azure2.png" alt="Logo" class="logo">
            <span class="fw-bold">Azura</span>
        </div>
        <div class="right">
            <!-- 알림 아이콘 (모달 trigger) -->
            <i class="bi bi-bell fs-5" role="button" data-bs-toggle="modal" data-bs-target="#notifModal"></i>

            <!-- 로그아웃 버튼 -->
            <form action="${pageContext.request.contextPath}/logout" method="post" style="display:inline;">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                <button type="submit" class="btn btn-outline-danger btn-sm">
                    로그아웃
                </button>
            </form>
        </div>
    </div>
</div>

<!-- 알림 모달 -->
<div class="modal fade" id="notifModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">알림</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body" id="notifList">
                아직 새로운 알림이 없습니다.
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7.0.0/bundles/stomp.umd.min.js"></script>


<!-- notification.js 포함 -->
<script src="${pageContext.request.contextPath}/js/notification.js"></script>

<script>
    window.APP_CTX = '${pageContext.request.contextPath}';
    const CURRENT_USER_ID = ${user.id};

    document.addEventListener("DOMContentLoaded", () => {
        if (CURRENT_USER_ID) {
            connectNotificationSocket(CURRENT_USER_ID);
        }
    });
</script>

<!-- Bootstrap Icons & JS -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">
<script src="${pageContext.request.contextPath}/js/bootstrap/bootstrap.bundle.js"></script>
</body>

