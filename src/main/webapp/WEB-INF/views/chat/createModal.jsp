<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- =========================================
     Chat Modal (대화방)
     - JS 로드는 mainbar.jsp에서만!
     - 여기에는 <script> 넣지 말 것
   ========================================= -->
<div id="chatModal" class="chat-modal" style="display:none;">
  <div class="chat-modal__backdrop" onclick="closeChatModal()"></div>

  <div class="chat-modal__panel" role="dialog" aria-modal="true">
    <!-- ===== 헤더 ===== -->
    <header class="chat-modal__header" style="display:flex; align-items:center; gap:12px;">
      <h3 id="chatHeaderTitle" style="margin:0; flex:1 1 auto;">대화방</h3>

      <!-- 번역 컨트롤 (헤더 오른쪽) -->
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

      <button type="button" class="btn btn-light" onclick="closeChatModal()">
        <i class="bi bi-x-lg"></i>
      </button>
    </header>

        <!-- ===== 본문 ===== -->
    <div class="chat-modal__body">
      <!-- 왼쪽: 채팅방/DM 목록 -->
      <aside class="chat-modal__sidebar">

        <!-- ⭐ 그룹 채팅 섹션 (토글 + 검색 + 리스트) -->
        <section class="chat-section">
          <button type="button"
                  class="btn btn-sm btn-link p-0 d-flex align-items-center"
                  onclick="toggleGroupList()">
            <i id="groupToggleIcon" class="bi bi-caret-down-fill me-1"></i>
            <span class="fw-bold">그룹 채팅</span>
          </button>

          <!-- 검색박스: 토글에 따라 show/hide -->
          <div id="groupSearchBox" class="mt-2">
            <input type="text"
                  id="groupSearchInput"
                  class="form-control form-control-sm"
                  placeholder="프로젝트 검색..."
                  oninput="filterGroup(this.value)" />
          </div>

          <!-- 서버에서 채워지는 프로젝트 리스트 -->
          <ul id="groupChatList" class="chat-list"></ul>
        </section>

        <!-- ⭐ DM 섹션 (기존 그대로) -->
        <section class="chat-section">
          <button type="button" class="btn btn-sm btn-link p-0 d-flex align-items-center"
                  onclick="toggleDMList()">
            <i id="dmToggleIcon" class="bi bi-caret-down-fill me-1"></i> DM
          </button>

          <div id="dmSearchBox" class="mt-2">
            <input type="text" class="form-control form-control-sm"
                  placeholder="사용자 검색..."
                  oninput="filterDM(this.value)" />
          </div>

          <ul id="dmList" class="dm-list"></ul>
        </section>
      </aside>

      <!-- 오른쪽: 메시지 영역 -->
      <section class="chat-modal__content">
        <div id="chatMessages" class="chat-messages"></div>

        <form class="chat-input mt-3" onsubmit="event.preventDefault(); sendMessage();">
          <label for="fileInput" class="file-attach" title="파일 첨부">
            <i class="bi bi-paperclip"></i>
          </label>
          <input type="file" id="fileInput" style="display:none;" />
          <input type="text" id="chatTextInput" class="form-control"
                placeholder="메시지를 입력하세요..." autocomplete="off" />
          <button id="btnChatSend" type="submit" class="btn btn-primary">전송</button>
        </form>
      </section>
    </div>
  </div>
</div>
