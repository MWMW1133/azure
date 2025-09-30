<!-- topbar.jsp -->
<%@ page contentType="text/html;charset=UTF-8" %>
    <!-- Bootstrap CSS -->
    <link href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.css" rel="stylesheet">
    <style>
        body {
            margin: 0;
            font-family: Arial, sans-serif;
        }
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
        .topbar .profile {
            width: 32px;
            height: 32px;
            border-radius: 50%;
        }

        .notif-panel {
            width: 340px;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            border: none;
            position: relative;
            padding: 16px;
        }
        .notif-panel::before {
            content: "";
            position: absolute;
            top: -10px;
            right: 10px;
            border-width: 0 10px 10px 10px;
            border-style: solid;
            border-color: transparent transparent #fff transparent;
            filter: drop-shadow(0 -1px 1px rgba(0,0,0,0.1));
        }
        /* 카드형 알림 */
        .notif-card {
            background: #f9f9f9;
            border-radius: 10px;
            padding: 10px 12px;
            margin-bottom: 12px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.08);
            font-size: 14px;
        }
        .notif-card:hover {
            background: #f1f1f1;
        }

        /* To-do 패널 공통 */
        .todo-panel {
            width: 500px;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            border: none;
            position: relative;
        }

        /* 표 스타일 */
        .todo-panel table {
            border-collapse: separate;
            border-spacing: 0 8px; /* 행 간격 */
        }

        .todo-panel thead th {
            font-size: 13px;
            color: #555;
            border-bottom: 1px solid #e5e5e5;
            padding-bottom: 6px;
        }

        .todo-panel tbody td {
            font-size: 13px;
            background: #f9f9f9;
            border-radius: 6px;
            padding: 8px 12px;
            vertical-align: middle;
        }

        /* 완료/미완료 라벨 */
        .badge {
            font-size: 12px;
            padding: 4px 8px;
            border-radius: 6px;
        }

        /* 채팅*/
        /* 모달 배경 */
        .modal-overlay {
            display: none; /* 기본적으로 숨김 */
            position: fixed;
            top: 0; left: 0;
            width: 100%; height: 100%;
            background: rgba(0, 0, 0, 0.5);
            z-index: 2000;
        }

        /* 모달 박스 */
        .modal-content {
            background: #fff;
            width: 1000px;
            height: 600px;
            margin: 80px auto;
            border-radius: 10px;
            display: flex;
            flex-direction: column;
        }

        /* 헤더 */
        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 15px;
            border-bottom: 1px solid #ddd;
        }

        /* 닫기 버튼을 모달 오른쪽 위에 고정 */
        .close-btn {
            background: none;
            border: none;
            font-size: 20px;
            color: #666;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .close-btn:hover {
            color: #000;
        }



        .modal-body {
            flex: 1;
            overflow-y: auto;
        }

        /* 모달 내부 2분할 */
        .chat-layout {
            display: flex;
            flex: 1;
            height: 100%;
        }

        /* 사이드바 */
        .chat-sidebar {
            width: 220px;
            border-right: 1px solid #ddd;
            padding: 15px;
            display: flex;
            flex-direction: column;
        }

        .chat-room-list {
            list-style: none;
            padding: 0;
            margin: 0;
        }

        .chat-room-list li {
            padding: 8px 12px;
            border-radius: 6px;
            cursor: pointer;
        }

        .chat-room-list li:hover {
            background: #f1f1f1;
        }

        .chat-room-list li.active {
            background: #e9f5ff;
            font-weight: bold;
        }

        /* 채팅창 */
        .chat-window {
            flex: 1;
            display: flex;
            flex-direction: column;
        }

        /* 채팅 헤더 (공용) */
        .chat-header {
            padding: 6px 12px;
            border-bottom: 1px solid #ddd;
            background: #fafafa;
            display: flex;
            justify-content: space-between;
            align-items: center;
            height: 50px;
        }

        .chat-header .left {
            font-weight: bold;
            font-size: 14px;
        }

        .chat-header .right {
            display: flex;
            flex-direction: row;
            align-items: center;
            gap: 10px;
        }

        .chat-header .right select {
            width: auto;          /* select 크기 자동 조절 */
            display: inline-block; /* 줄바꿈 방지 */
            padding-right: 24px;
            min-width: 80px;
        }


        .chat-header .btn,
        .chat-header select {
            font-size: 12px;
            padding: 2px 6px;
            height: 28px;           /* 버튼 높이를 자동으로 */
            line-height: 1.4;       /* 세로 균형 맞춤 */
        }

        .chat-messages {
            flex: 1;
            padding: 15px;
            overflow-y: auto;
            background: #fff;
        }

        .message {
            margin-bottom: 10px;
            padding: 8px 12px;
            border-radius: 12px;
            max-width: 70%;
        }

        .message.left {
            background: #f1f1f1;
            align-self: flex-start;
        }

        .message.right {
            background: #d1e7ff;
            align-self: flex-end;
        }

        /* 입력창 */
        .chat-input {
            display: flex;
            align-items: center;
            border-top: 1px solid #ddd;
            padding: 10px;
            gap: 8px;
        }

        .chat-input .file-attach {
            cursor: pointer;
            font-size: 18px;
            color: #555;
        }

        .chat-input .file-attach:hover {
            color: #000;
        }

        .chat-input input[type="text"] {
            flex: 1;
            border: 1px solid #ccc;
            border-radius: 6px;
            padding: 8px;
        }

        .chat-header .form-switch {
            margin: 0;
            padding: 0;
            display: flex;
            align-items: center;
        }

        .chat-header .form-check-input {
            cursor: pointer;
        }


        /* 번역 토글 */
        .translate-toggle {
            display: flex;
            align-items: center;
            gap: 6px;            /* 글자와 토글 사이 간격 */
        }

        .translate-toggle label {
            font-size: 13px;     /* 글자 조금 작게 */
            margin: 0;
            cursor: pointer;     /* 클릭 가능하게 */
        }

        .translate-toggle .form-check-input {
            width: 32px;         /* 토글 크기 살짝 키움 */
            height: 18px;
            cursor: pointer;
        }



    </style>
</head>
<body>

    <!-- 탑바 -->
    <div class="topbar">
        <div class="left">
            <img src="${pageContext.request.contextPath}/images/logo/azure2.png" alt="Logo" class="logo">
            <span class="fw-bold">Azura</span>
        </div>
        <div class="right">

            <!-- To-do -->
            <div class="dropdown">
                <i class="bi bi-check2-square fs-5" role="button" id="todoDropdown" data-bs-toggle="dropdown"></i>
                <div class="dropdown-menu dropdown-menu-end p-3 todo-panel" aria-labelledby="todoDropdown"
                     style="max-height:400px; overflow-y:auto;">
                    <div class="fw-bold mb-2">To-do List</div>
                    <table class="table table-sm align-middle">
                        <thead>
                        <tr>
                            <th>✔</th>
                            <th>일정 제목</th>
                            <th>일정 시간</th>
                            <th>완료 여부</th>
                            <th>메모</th>
                        </tr>
                        </thead>
                        <tbody id="todoList"></tbody>
                    </table>
                </div>
            </div>


            <!-- 알림 드롭다운 -->
            <div class="dropdown">
                <i class="bi bi-bell fs-5" role="button" id="notifDropdown" data-bs-toggle="dropdown" aria-expanded="false"></i>

                <div class="dropdown-menu dropdown-menu-end p-3 notif-panel" aria-labelledby="notifDropdown">
                    <div class="fw-bold mb-2">Notification</div>

                    <!-- 알림 항목 렌더링 영역(notificaton.js 참고) -->
                    <div id="notifList"></div>

                    <div class="text-center"><a href="#" class="small">모든 알림 보기</a></div>
                </div>
            </div>

            <!-- 채팅 아이콘 -->
            <div class="nav-item">
                <a href="javascript:void(0)" class="nav-link" onclick="openChatModal()">
                    <i class="bi bi-chat-dots"></i>
                </a>
            </div>

            <!-- 메인 채팅 모달 -->
            <div id="chatModal" class="modal-overlay">
                <div class="modal-content">

                    <div class="modal-body chat-layout">
                        <!-- 사이드바 -->
                        <div class="chat-sidebar">
                            <button class="btn btn-success w-100 mb-3">+ 그룹 채팅방 생성</button>
                            <div class="fw-bold mb-2">목록</div>
                            <ul class="chat-room-list">
                                <li class="active">프로젝트 1</li>
                                <li>프로젝트 2</li>
                                <li>프로젝트 3</li>
                            </ul>
                        </div>

                        <!-- 채팅창 -->
                        <div class="chat-window">
                            <!-- 공용 헤더 -->
                            <div class="chat-header">
                                <div class="left">프로젝트 1</div>
                                <div class="right">
<%--                                    <div class="form-check form-switch">--%>
<%--                                        <input class="form-check-input" type="checkbox" id="translateToggle">--%>
<%--                                        <label class="form-check-label" for="translateToggle">번역</label>--%>
<%--                                    </div>--%>

                                    <div class="translate-toggle">
                                        <label for="translateToggle">번역</label>
                                        <input class="form-check-input" type="checkbox" id="translateToggle">
                                    </div>
                                    <select class="form-select form-select-sm">
                                        <option>한국어</option>
                                        <option>영어</option>
                                        <option>중국어</option>
                                        <option>일본어</option>
                                    </select>
                                    <button class="btn btn-primary btn-sm">요약</button>

                                    <!-- 닫기 버튼-->
                                    <button type="button" class="close-btn" onclick="closeChatModal()">
                                        <i class="bi bi-x"></i>
                                    </button>
                                </div>
                            </div>

                            <!-- 메시지 영역 -->
                            <div class="chat-messages">
                                <div class="message left">안녕하세요 👋</div>
                                <div class="message right">네 반가워요!</div>
                            </div>

                            <!-- 입력창 -->
                            <div class="chat-input">
                                <!-- 파일 첨부 아이콘 -->
                                <label for="fileInput" class="file-attach">
                                    <i class="bi bi-paperclip"></i>
                                </label>
                                <input type="file" id="fileInput" style="display: none;" />

                                <input type="text" placeholder="메시지를 입력하세요..." />
                                <button class="btn btn-primary">전송</button>
                            </div>
                        </div>
                    </div>

                </div>
            </div>


            <i class="bi bi-search" role="button"></i>
            <img src="${pageContext.request.contextPath}/images/my-cat.png" alt="Profile" class="profile">
        </div>
    </div>

<!-- Bootstrap Icons & JS -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">
<script src="${pageContext.request.contextPath}/js/bootstrap/bootstrap.bundle.js"></script>
<script src="${pageContext.request.contextPath}/js/notification.js"></script>
<script src="${pageContext.request.contextPath}/js/todo.js"></script>
<script src="${pageContext.request.contextPath}/js/chat/modal.js"></script>


</body>
