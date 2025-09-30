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
            gap: 1rem;  /* 아이콘 간격 */
        }

        .topbar .right i {
            cursor: pointer;
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

        /* 검색 관련 */
        .search-modal { border-radius: 1rem; }
        #searchTabContent { max-height: 48vh; overflow: auto; }
        .notif-panel, .todo-panel { width: 380px; }


        /* 프로필 클릭 시 */
        /* 프로필 드롭다운 전체 */
        .dropdown-menu.profile-menu {
            min-width: 240px;
            border-radius: 12px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.1);
            padding: 12px;
        }

        /* 상단 프로필 영역 */
        .profile-header {
            display: flex;
            align-items: center;
            padding: 8px 0;
        }

        .profile-header img {
            width: 48px;
            height: 48px;
            border-radius: 50%;
            margin-right: 10px;
        }

        .profile-header .name {
            font-weight: 600;
            font-size: 15px;
        }

        /* 메뉴 아이템 */
        .profile-menu .dropdown-item {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 8px 12px;
            font-size: 14px;
        }

        .profile-menu .dropdown-item i {
            font-size: 16px;
        }

        /* 로그아웃은 빨간색 */
        .profile-menu .dropdown-item.logout {
            color: #d93025;
            font-weight: 500;
        }
        .profile-menu .dropdown-item.logout:hover {
            background-color: #fce8e6;
            color: #b31412;
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

        <div class="right d-flex align-items-center gap-3">

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
<<<<<<< HEAD
                    <i class="bi bi-chat-dots fs-5"></i>
                </a>
            </div>


            <!-- 검색 아이콘 -->
            <a href="javascript:void(0)"
               id="openSearchIcon"
               data-bs-toggle="modal"
               data-bs-target="#globalSearchModal"
               class="ms-2">
                <i class="bi bi-search fs-5 text-dark" role="button" aria-label="검색"></i>
            </a>

            <!-- 프로필 드롭다운 --> <!-- 더미 데이터라서 나중에 변경 -->
            <div class="dropdown">
                <img src="${pageContext.request.contextPath}/images/my-cat.png"
                     alt="Profile" class="profile dropdown-toggle" id="profileDropdown"
                     data-bs-toggle="dropdown" aria-expanded="false">
<%--                <img src="$src="${user.avatarUrl}"--%>
<%--                     alt="Profile" class="profile dropdown-toggle" id="profileDropdown"--%>
<%--                     data-bs-toggle="dropdown" aria-expanded="false">--%>

                <ul class="dropdown-menu dropdown-menu-end profile-menu" aria-labelledby="profileDropdown">
                    <!-- 프로필 헤더 --> <!-- 더미 데이터라서 나중에 변경 -->
                    <li class="profile-header">
                        <img src="${pageContext.request.contextPath}/images/my-cat.png" alt="Profile">
<%--                        <img src="${user.avatarUrl}" alt="Profile">--%>
                        <div class="name">박소현</div>
<%--                        <div class="name">${user.name}</div>--%>
                    </li>
                    <hr class="my-2">

                    <!-- 메뉴 아이템 -->
                    <li><a class="dropdown-item" href="${pageContext.request.contextPath}/profile"><i class="bi bi-person"></i>View profile</a></li>
                    <li><a class="dropdown-item" href="#"><i class="bi bi-gear"></i>Settings</a></li>
                    <li><a class="dropdown-item" href="#"><i class="bi bi-people"></i>Invite colleagues</a></li>
                    <li><a class="dropdown-item logout" href="#"><i class="bi bi-box-arrow-right"></i>Log out</a></li>
                </ul>
            </div>
        </div>
    </div>
    <!-- 메인 채팅 모달 -->
    <div id="chatModal" class="modal-overlay">
        <div class="modal-content">

            <div class="modal-body chat-layout">
                <!-- 사이드바 -->
                <div class="chat-sidebar">
                    <button class="btn btn-success w-100 mb-3" onclick="openCreateChatModal()">
                        + 그룹 채팅방 생성
                    </button>

                    <!-- 그룹 채팅 -->
                    <div class="fw-bold mt-3">
                        <i class="bi bi-people-fill me-1"></i> 그룹 채팅
                    </div>
                    <!-- TODO: JS에서 프로젝트 목록(li) 주입 -->
                    <ul id="groupChatList" class="chat-room-list"></ul>

                    <!-- DM 섹션 -->
                    <div class="section-header d-flex justify-content-between align-items-center"
                         onclick="toggleDMList()">
                    <span>
                        <i class="bi bi-person-fill me-1"></i> DM
                    </span>
                        <i id="dmToggleIcon" class="bi bi-caret-down-fill"></i>
                    </div>

                    <!-- DM 검색창 -->
                    <div id="dmSearchBox" class="mt-2" style="display:none;">
                        <input type="text" class="form-control form-control-sm"
                               placeholder="사용자 검색..." onkeyup="filterDM(this.value)">
                    </div>

                    <!-- TODO: JS에서 DM 목록(li) 주입 -->
                    <ul id="dmList" class="chat-room-list mt-2">
                        <%-- <li>홍길동</li><li>김철수</li> --%>
                    </ul>
                </div>

                <!-- 채팅창 -->
                <div class="chat-window">
                    <!-- 공용 헤더 -->
                    <div class="chat-header">
                        <!-- 사이드바 목록에서 선택한거 헤더에 그대로 들고오기 -->
                        <div class="left" id="chatHeaderTitle"></div><!--  여기에 반영 -->

                        <!-- 가운데 번역 영역 -->
                        <div class="center">
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
                        </div>

                        <div class="right">
                            <button class="btn btn-primary btn-sm">요약</button>
                            <button type="button" class="close-btn" onclick="closeChatModal()">
                                <i class="bi bi-x"></i>
                            </button>
                        </div>
                    </div>

                    <!-- 메시지 영역 -->
                    <div class="chat-messages" id="chatMessages">
                        <!-- TODO: JS에서 메시지 렌더링 -->
                    </div>

                    <!-- 입력창 -->
                    <div class="chat-input">
                        <label for="fileInput" class="file-attach">
                            <i class="bi bi-paperclip"></i>
                        </label>
                        <input type="file" id="fileInput" style="display: none;" />

                        <input type="text" id="chatInput" placeholder="메시지를 입력하세요..." />
                        <button class="btn btn-primary" onclick="sendMessage()">전송</button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- 검색 모달 -->
    <div class="modal fade" id="globalSearchModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-xl">
            <div class="modal-content search-modal">
                <div class="modal-header border-0 pb-0">
                    <h5 class="modal-title fw-semibold">통합 검색</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="닫기"></button>
                </div>

                <div class="modal-body pt-2">
                    <!-- 검색 인풋 -->
                    <div class="input-group mb-3">
                        <span class="input-group-text"><i class="bi bi-search"></i></span>
                        <input id="searchInput" type="text" class="form-control" placeholder="프로젝트, 태스크, 파일, 구성원 검색">
                        <button id="searchClear" class="btn btn-outline-secondary" type="button" title="지우기">
                            <i class="bi bi-x-lg"></i>
                        </button>
                    </div>

                    <!-- 탭 -->
                    <ul class="nav nav-tabs small" id="searchTabs" role="tablist">
                        <li class="nav-item" role="presentation">
                            <button class="nav-link active" id="tab-all" data-bs-toggle="tab" data-bs-target="#pane-all" type="button" role="tab">모두</button>
                        </li>
                        <li class="nav-item" role="presentation">
                            <button class="nav-link" id="tab-project" data-bs-toggle="tab" data-bs-target="#pane-project" type="button" role="tab">프로젝트</button>
                        </li>
                        <li class="nav-item" role="presentation">
                            <button class="nav-link" id="tab-task" data-bs-toggle="tab" data-bs-target="#pane-task" type="button" role="tab">태스크</button>
                        </li>
                        <li class="nav-item" role="presentation">
                            <button class="nav-link" id="tab-file" data-bs-toggle="tab" data-bs-target="#pane-file" type="button" role="tab">파일</button>
                        </li>
                        <li class="nav-item" role="presentation">
                            <button class="nav-link" id="tab-member" data-bs-toggle="tab" data-bs-target="#pane-member" type="button" role="tab">구성원</button>
                        </li>
                    </ul>

                    <!-- 본문: 좌 결과 / 우 필터 -->
                    <div class="row mt-3">
                        <!-- 좌측: 결과 -->
                        <div class="col-12 col-lg-8">
                            <div class="tab-content" id="searchTabContent" style="min-height:320px;">
                                <!-- 탭: 모두 -->
                                <div class="tab-pane fade show active" id="pane-all" role="tabpanel" aria-labelledby="tab-all">
                                    <ul id="result-all" class="list-group list-group-flush small">
                                        <!-- 더미: UI 확인용 -->
                                        <li class="list-group-item d-flex justify-content-between align-items-center">
                                            <div>
                                                <div class="fw-semibold"></div>
                                                <div class="text-muted"></div>
                                            </div>
                                            <div class="text-muted"></div>
                                        </li>
                                    </ul>
                                </div>

                                <!-- 탭: 프로젝트 -->
                                <div class="tab-pane fade" id="pane-project" role="tabpanel" aria-labelledby="tab-project">
                                    <ul id="result-project" class="list-group list-group-flush small"></ul>
                                </div>

                                <!-- 탭: 태스크 -->
                                <div class="tab-pane fade" id="pane-task" role="tabpanel" aria-labelledby="tab-task">
                                    <ul id="result-task" class="list-group list-group-flush small"></ul>
                                </div>

                                <!-- 탭: 파일 -->
                                <div class="tab-pane fade" id="pane-file" role="tabpanel" aria-labelledby="tab-file">
                                    <ul id="result-file" class="list-group list-group-flush small"></ul>
                                </div>

                                <!-- 탭: 구성원 -->
                                <div class="tab-pane fade" id="pane-member" role="tabpanel" aria-labelledby="tab-member">
                                    <ul id="result-member" class="list-group list-group-flush small"></ul>
                                </div>
                            </div>
                        </div>

                        <!-- 우측: 검색 기준(필터) -->
                        <div class="col-12 col-lg-4 mt-4 mt-lg-0">
                            <div class="border rounded p-3">
                                <div class="d-flex align-items-center justify-content-between">
                                    <div class="fw-semibold">검색 기준</div>
                                </div>
                                <hr class="my-2">
                                <div class="small text-muted mb-1">검색 결과를 선택한 옵션 기준으로 필터링합니다.</div>
                                <div class="list-group list-group-flush mb-2 small">
                                    <button class="list-group-item list-group-item-action">대시보드 상태</button>
                                    <button class="list-group-item list-group-item-action">우선순위</button>
                                    <button class="list-group-item list-group-item-action">작성자 또는 담당자</button>
                                </div>
                                <div class="mb-3">
                                <label for="sortSelect" class="small text-muted mb-1">정렬</label>
                                <select id="sortSelect" class="form-select form-select-sm">
                                    <option value="deadline">마감일 임박순</option>
                                    <option value="recent">최근 수정일</option>
                                    <option value="name">이름순</option>
                                </select>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="modal-footer border-0">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                    <button id="searchRun" type="button" class="btn btn-primary">검색</button>
                </div>
            </div>
        </div>
    </div>

<!-- Bootstrap Icons & JS -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">
<script src="${pageContext.request.contextPath}/js/bootstrap/bootstrap.bundle.js"></script>
<script src="${pageContext.request.contextPath}/js/notification.js"></script>
<script src="${pageContext.request.contextPath}/js/todo.js"></script>
<script src="${pageContext.request.contextPath}/js/chat/modal.js"></script>


</body>
