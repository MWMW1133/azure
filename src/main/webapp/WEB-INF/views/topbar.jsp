<%@ page contentType="text/html;charset=UTF-8" %>

<!-- 탑바 -->
<div class="topbar">
  <div class="left">
    <img src="${pageContext.request.contextPath}/images/logo/azure2.png" alt="Logo" class="logo">
    <span class="fw-bold">Azura</span>
  </div>

  <div class="right d-flex align-items-center gap-3">

    <!-- ✅ To-do 드롭다운 -->
    <div class="dropdown">
      <i class="bi bi-check2-square fs-5" role="button" id="todoDropdown"
         data-bs-toggle="dropdown" data-bs-display="static" aria-expanded="false"></i>

      <div class="dropdown-menu dropdown-menu-end p-3" aria-labelledby="todoDropdown" style="min-width: 700px;">
        <div class="fw-bold mb-2">오늘의 To-do</div>
        <div class="table-responsive">
          <table class="table table-sm align-middle mb-2">
            <thead>
              <tr>
                <th style="width:36px;"></th>
                <th>제목</th>
                <th style="width:200px;">시간</th>
                <th style="width:80px;">상태</th>
                <th style="width:180px;">메모</th>
              </tr>
            </thead>
            <tbody id="todoList">
              <tr><td colspan="5" class="text-muted">불러오는 중…</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 알림 드롭다운 -->
    <div class="dropdown">
      <i class="bi bi-bell fs-5" role="button" id="notifDropdown" data-bs-toggle="dropdown"
         data-bs-display="static" aria-expanded="false"></i>

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

    <!-- 검색 아이콘 -->
    <a href="javascript:void(0)" id="openSearchIcon" data-bs-toggle="modal"
       data-bs-target="#globalSearchModal" class="ms-2">
      <i class="bi bi-search fs-5 text-dark" role="button" aria-label="검색"></i>
    </a>

    <!-- 프로필 드롭다운 -->
    <div class="dropdown">
      <img src="${pageContext.request.contextPath}${user.avatarUrl}" alt="Profile"
           class="profile dropdown-toggle" id="profileDropdown"
           data-bs-toggle="dropdown" data-bs-display="static" aria-expanded="false">

      <ul class="dropdown-menu dropdown-menu-end profile-menu" aria-labelledby="profileDropdown">
        <li class="profile-header">
          <img src="${pageContext.request.contextPath}${user.avatarUrl}" alt="Profile">
          <div class="name">${user.name}</div>
        </li>
        <hr class="my-2">
        <li><a class="dropdown-item" href="${pageContext.request.contextPath}/profile"><i class="bi bi-person"></i>View profile</a></li>

        <li class="dropdown-submenu">
          <a class="dropdown-item" href="#">
            <i class="bi bi-gear"></i> Settings
          </a>
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
          <a class="dropdown-item" href="#" id="openInviteModal">
            <i class="bi bi-people"></i> Invite colleagues
          </a>
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



