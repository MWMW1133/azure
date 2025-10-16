<%@ page contentType="text/html;charset=UTF-8" %>
<!-- Bootstrap CSS -->
<link href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.css" rel="stylesheet">

<style>
  /* ====== 기존 스타일 그대로 ====== */
  body { margin:0; font-family:Arial, sans-serif; }
  .topbar { position:sticky; top:0; z-index:1000; height:60px; background:#fff; border-bottom:1px solid #ddd;
            display:flex; align-items:center; justify-content:space-between; padding:0 20px; }
  .topbar .left { display:flex; align-items:center; gap:8px; }
  .topbar .logo { height:28px; width:auto; }
  .topbar .right { display:flex; align-items:center; gap:18px; }
  .topbar .profile { width:32px; height:32px; border-radius:50%; }

  .notif-panel { width:340px; border-radius:12px; box-shadow:0 4px 12px rgba(0,0,0,0.15); border:none; position:relative; padding:16px; }
  .notif-panel::before { content:""; position:absolute; top:-10px; right:10px; border-width:0 10px 10px 10px; border-style:solid;
                         border-color:transparent transparent #fff transparent; filter:drop-shadow(0 -1px 1px rgba(0,0,0,0.1)); }
  .notif-card { background:#f9f9f9; border-radius:10px; padding:10px 12px; margin-bottom:12px; box-shadow:0 1px 3px rgba(0,0,0,0.08); font-size:14px; }
  .notif-card:hover { background:#f1f1f1; }

  .todo-panel { width:500px; border-radius:12px; box-shadow:0 4px 12px rgba(0,0,0,0.15); border:none; position:relative; }
  .todo-panel table { border-collapse:separate; border-spacing:0 8px; }
  .todo-panel thead th { font-size:13px; color:#555; border-bottom:1px solid #e5e5e5; padding-bottom:6px; }
  .todo-panel tbody td { font-size:13px; background:#f9f9f9; border-radius:6px; padding:8px 12px; vertical-align:middle; }
  .badge { font-size:12px; padding:4px 8px; border-radius:6px; }

  /* ====== 채팅 모달 ====== */
  .modal-overlay { display:none; position:fixed; inset:0; background:rgba(0,0,0,0.5); z-index:2000; }
  .modal-content { background:#fff; width:1000px; height:600px; margin:80px auto; border-radius:10px; display:flex; flex-direction:column; }
  .modal-body { flex:1; overflow:hidden; }
  .chat-layout { display:flex; height:100%; }

  .chat-sidebar { width:220px; border-right:1px solid #ddd; padding:15px; display:flex; flex-direction:column; }
  .chat-room-list { list-style:none; padding:0; margin:0; }
  .chat-room-list li { padding:8px 12px; border-radius:6px; cursor:pointer; }
  .chat-room-list li:hover { background:#f1f1f1; }
  .chat-room-list li.active { background:#e9f5ff; font-weight:bold; }

  .chat-window { flex:1; display:flex; flex-direction:column; }
  .chat-header { padding:6px 12px; border-bottom:1px solid #ddd; background:#fafafa; display:flex; justify-content:space-between; align-items:center; height:50px; }
  .chat-header .left { font-weight:bold; font-size:14px; }
  .chat-header .right { display:flex; align-items:center; gap:10px; }
  .chat-header .btn, .chat-header select { font-size:12px; padding:2px 6px; height:28px; line-height:1.4; }
  .chat-header select { width:auto; display:inline-block; min-width:92px; }

  .chat-messages { flex:1; padding:15px; overflow-y:auto; background:#fff; }
  .message { margin-bottom:10px; padding:8px 12px; border-radius:12px; max-width:70%; }
  .message.left { background:#f1f1f1; align-self:flex-start; }
  .message.right { background:#d1e7ff; align-self:flex-end; }

  .chat-input { display:flex; align-items:center; border-top:1px solid #ddd; padding:10px; gap:8px; }
  .chat-input .file-attach { cursor:pointer; font-size:18px; color:#555; }
  .chat-input .file-attach:hover { color:#000; }
  .chat-input input[type="text"] { flex:1; border:1px solid #ccc; border-radius:6px; padding:8px; }

  .close-btn { background:none; border:none; font-size:20px; color:#666; cursor:pointer; display:flex; align-items:center; justify-content:center; }
  .close-btn:hover { color:#000; }
</style>

<body>
  <!-- ===== Topbar ===== -->
  <div class="topbar">
    <div class="left">
      <img src="${pageContext.request.contextPath}/images/logo/azure2.png" alt="Logo" class="logo">
      <span class="fw-bold">Azura</span>
    </div>

    <div class="right">
      <!-- To-do -->
      <div class="dropdown">
        <i class="bi bi-check2-square fs-5" role="button" id="todoDropdown" data-bs-toggle="dropdown" data-bs-display="static"></i>
        <div class="dropdown-menu dropdown-menu-end p-3 todo-panel" aria-labelledby="todoDropdown" style="max-height:400px; overflow-y:auto;">
          <div class="fw-bold mb-2">To-do List</div>
          <table class="table table-sm align-middle">
            <thead><tr><th>✔</th><th>일정 제목</th><th>일정 시간</th><th>완료 여부</th><th>메모</th></tr></thead>
            <tbody id="todoList"></tbody>
          </table>
        </div>
      </div>

      <!-- 알림 -->
      <div class="dropdown">
        <i class="bi bi-bell fs-5" role="button" id="notifDropdown" data-bs-toggle="dropdown" data-bs-display="static" aria-expanded="false"></i>
        <div class="dropdown-menu dropdown-menu-end p-3 notif-panel" aria-labelledby="notifDropdown">
          <div class="fw-bold mb-2">Notification</div>
          <div id="notifList"></div>
          <div class="text-center"><a href="#" class="small">모든 알림 보기</a></div>
        </div>
      </div>

      <!-- 채팅 아이콘 -->
      <div class="nav-item">
        <a href="javascript:void(0)" class="nav-link" onclick="openChatModal()">
          <i class="bi bi-chat-dots fs-5"></i>
        </a>
      </div>

      <!-- 검색 -->
      <a href="javascript:void(0)" id="openSearchIcon" data-bs-toggle="modal" data-bs-target="#globalSearchModal" class="ms-2">
        <i class="bi bi-search fs-5 text-dark" role="button" aria-label="검색"></i>
      </a>

      <!-- 프로필 -->
      <div class="dropdown">
        <img src="${pageContext.request.contextPath}${user.avatarUrl}" alt="Profile" class="profile dropdown-toggle"
             id="profileDropdown" data-bs-toggle="dropdown" data-bs-display="static" aria-expanded="false">
        <ul class="dropdown-menu dropdown-menu-end profile-menu" aria-labelledby="profileDropdown">
          <li class="profile-header">
            <img src="${pageContext.request.contextPath}${user.avatarUrl}" alt="Profile">
            <div class="name">${user.name}</div>
          </li>
          <hr class="my-2">
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/profile"><i class="bi bi-person"></i>View profile</a></li>
          <li class="dropdown-submenu">
            <a class="dropdown-item" href="#"><i class="bi bi-gear"></i> Settings</a>
            <ul class="dropdown-menu">
              <li>
                <a class="dropdown-item" data-bs-toggle="collapse" href="#accountSubmenu" role="button" aria-expanded="false" aria-controls="accountSubmenu">
                  <i class="bi bi-person"></i> Account Settings ▼
                </a>
                <div class="collapse" id="accountSubmenu">
                  <a class="dropdown-item change-password" href="#"><i class="bi bi-key"></i> Change Password</a>
                </div>
              </li>
              <li>
                <a class="dropdown-item" data-bs-toggle="collapse" href="#notifSubmenu" role="button" aria-expanded="false" aria-controls="notifSubmenu">
                  <i class="bi bi-bell"></i> Notification Settings ▼
                </a>
                <div class="collapse" id="notifSubmenu">
                  <a class="dropdown-item" href="#"><i class="bi bi-toggle-on"></i> Push Notifications</a>
                </div>
              </li>
            </ul>
          </li>
          <li>
            <a class="dropdown-item" href="#" id="openInviteModal"><i class="bi bi-people"></i> Invite colleagues</a>
          </li>
          <li>
            <form action="${pageContext.request.contextPath}/logout" method="post" style="display:inline;">
              <button type="submit" class="dropdown-item logout" style="border:none; background:none; width:100%; text-align:left;">
                <i class="bi bi-box-arrow-right"></i> Log out
              </button>
            </form>
          </li>
        </ul>
      </div>
    </div>
  </div>

  <!-- ========= 채팅 모달 (단 하나만 유지) ========= -->
  <div id="chatModal" class="modal-overlay">
    <div class="modal-content">
      <div class="modal-body chat-layout">
        <!-- 사이드바 -->
        <aside class="chat-sidebar">
          

          <div class="fw-bold mt-3"><i class="bi bi-people-fill me-1"></i> 그룹 채팅</div>
          <ul id="groupChatList" class="chat-room-list"></ul>

          <div class="section-header d-flex justify-content-between align-items-center" onclick="toggleDMList()">
            <span><i class="bi bi-person-fill me-1"></i> DM</span>
            <i id="dmToggleIcon" class="bi bi-caret-down-fill"></i>
          </div>

          <div id="dmSearchBox" class="mt-2" style="display:none;">
            <input type="text" class="form-control form-control-sm" placeholder="사용자 검색..." onkeyup="filterDM(this.value)">
          </div>

          <ul id="dmList" class="chat-room-list mt-2"></ul>
        </aside>

        <!-- 우측 대화창 -->
        <section class="chat-window">
          <header class="chat-header">
            <div class="left" id="chatHeaderTitle">대화방</div>
            <div class="right">
              <!-- ✅ 번역 컨트롤: 모달에만 존재, ID 고정 -->
              <label class="d-flex align-items-center" style="gap:6px; margin:0;">
                <input class="form-check-input" type="checkbox" id="mt-enable"> 번역
              </label>

              <select id="mt-target" class="form-select form-select-sm">
                <option value="ko">한국어</option>
                <option value="en">영어</option>
                <option value="zh">중국어</option>
                <option value="ja">일본어</option>
              </select>

              <button type="button" class="close-btn" onclick="closeChatModal()">
                <i class="bi bi-x"></i>
              </button>
            </div>
          </header>

          <!-- 메시지 영역 -->
          <div class="chat-messages" id="chatMessages"></div>

          <!-- 입력창 -->
          <div class="chat-input">
            <!-- ✅ client.js가 찾는 ID로 통일 -->
            <input type="text" id="chatTextInput" placeholder="메시지를 입력하세요...">
            <button id="btnChatSend" class="btn btn-primary">전송</button>
          </div>
        </section>
      </div>
    </div>
  </div>
  <!-- ========= /채팅 모달 ========= -->

  <!-- 검색 모달(기존 그대로) -->
  <div class="modal fade" id="globalSearchModal" tabindex="-1" aria-hidden="true">
    <!-- ... (생략: 기존 검색 모달 내용 그대로 유지) ... -->
  </div>

  <!-- 전역 값 주입 -->
  <script>
    window.APP_CTX = '${pageContext.request.contextPath}';
    const userRole = "${org.role}";
    const CURRENT_USER_ID = ${user.id};
  </script>

  <!-- SockJS/STOMP -->
  <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7.0.0/bundles/stomp.umd.min.js"></script>

  <%-- ❌ (중복 UI) 기존 다른 위치의 번역 토글/셀렉트는 전부 삭제/주석 처리하세요.
      예: topbar 안의 번역 토글, createModal.jsp의 또 다른 모달 등 --%>
</body>
