<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- =========================================
     Chat Modal (대화방)
     - JS 로드는 mainbar.jsp에서만!
     - 여기에는 <script> 넣지 말 것
   ========================================= -->
<div id="chatModal" class="chat-modal" style="display:none;">
  <div class="chat-modal__backdrop" onclick="closeChatModal()"></div>

  <div class="chat-modal__panel" role="dialog" aria-modal="true">
    <header class="chat-modal__header">
      <h3 id="chatHeaderTitle">대화방</h3>
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

        <!-- DM 토글 & 검색 -->
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

          <ul id="dmList" class="chat-list mt-2"></ul>
        </section>

      </aside>

      <!-- 오른쪽: 메시지 영역 -->
      <section class="chat-modal__content">
        <div id="chatMessages" class="chat-messages"></div>

        <form class="chat-input mt-3" onsubmit="return false;">
          <div class="row g-2">
            <div class="col-auto">
              <select id="projectSelect" class="form-select form-select-sm"></select>
            </div>
            <div class="col">
              <input id="chatTextInput" type="text" class="form-control form-control-sm" placeholder="메시지 입력…" />
            </div>
            <div class="col-auto">
              <button type="button" class="btn btn-primary btn-sm"
                      onclick="/* TODO: send handler 연결 */ null">
                보내기
              </button>
            </div>
          </div>
        </form>
      </section>
    </div>
  </div>
</div>

<!-- =========================================
     Create Chat Modal (채팅방 생성)
   ========================================= -->
<div id="createChatModal" class="chat-modal" style="display:none;">
  <div class="chat-modal__backdrop" onclick="closeCreateChatModal()"></div>

  <div class="chat-modal__panel" role="dialog" aria-modal="true">
    <header class="chat-modal__header">
      <h3>채팅방 만들기</h3>
      <button type="button" class="btn btn-light" onclick="closeCreateChatModal()">
        <i class="bi bi-x-lg"></i>
      </button>
    </header>

    <div class="chat-modal__body">
      <form onsubmit="return false;">
        <div class="mb-3">
          <label class="form-label">방 이름</label>
          <input type="text" class="form-control" id="createRoomName" placeholder="예: 프로젝트 A 회의방" />
        </div>

        <div class="mb-3">
          <label class="form-label">참여자(쉼표로 구분)</label>
          <input type="text" class="form-control" id="createRoomMembers" placeholder="예: 홍길동, 김철수" />
        </div>

        <div class="d-flex gap-2 justify-content-end">
          <button type="button" class="btn btn-outline-secondary" onclick="closeCreateChatModal()">취소</button>
          <button type="button" class="btn btn-primary"
                  onclick="/* TODO: 생성 API 연동 */ closeCreateChatModal()">생성</button>
        </div>
      </form>
    </div>
  </div>
</div>
