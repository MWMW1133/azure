<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- =========================================
     Chat Modal (대화방)
     - JS 로드는 mainbar.jsp에서만!
     - 여기에는 <script> 넣지 말 것
   ========================================= -->
<div id="chatModal" class="chat-modal" style="display:none;">
  <div class="chat-modal__backdrop" onclick="closeChatModal()"></div>

  <div class="chat-modal__panel" role="dialog" aria-modal="true">
    <header class="chat-modal__header" style="display:flex; align-items:center; gap:12px;">
      <h3 id="chatHeaderTitle" style="margin:0; flex:1 1 auto;">대화방</h3>

      <!-- ▼▼▼ 번역 컨트롤 (헤더 오른쪽) -->
      <div class="d-flex align-items-center gap-2" style="flex:0 0 auto;">
        <label class="me-2 d-flex align-items-center" style="gap:6px; margin:0;">
          <input type="checkbox" id="mt-enable"> 번역
        </label>

        <select id="mt-target" class="form-select form-select-sm" style="width:auto; display:inline-block">
          <option value="en">영어</option>
          <option value="ko">한국어</option>
          <option value="ja">일본어</option>
          <option value="zh">중국어</option>
        </select>
      </div>
      <!-- ▲▲▲ -->

      <button type="button" class="btn btn-light" onclick="closeChatModal()">
        <i class="bi bi-x-lg"></i>
      </button>
    </header>

    <div class="chat-modal__body">
      <!-- 왼쪽: 채팅방/DM 목록 -->
      <aside class="chat-modal__sidebar">

        <!-- 그룹 채팅 목록 -->
        <section class="chat-section">
          <div class="chat-section__title">그룹 채팅</div>
          <ul id="groupChatList" class="chat-list"></ul>
        </section>

        <!-- DM 섹션 -->
        <section class="chat-section">
          <button type="button" class="btn btn-sm btn-link p-0 d-flex align-items-center"
                  onclick="toggleDMList()">
            <i id="dmToggleIcon" class="bi bi-caret-down-fill me-1"></i> DM
          </button>

          <div id="dmSearchBox" class="mt-2">
            <input type="text" class="form-control form-control-sm"
                   placeholder="이름 검색"
                   oninput="filterDM(this.value)" />
          </div>

          <!-- ✅ 팀원 목록은 JS가 여기(#dmList)에만 동적으로 렌더링 -->
          <ul id="dmList" class="dm-list"></ul>
        </section>

      </aside>

      <!-- 오른쪽: 메시지 영역 -->
      <section class="chat-modal__content">
        <div id="chatMessages" class="chat-messages"></div>

        <form class="chat-input mt-3" onsubmit