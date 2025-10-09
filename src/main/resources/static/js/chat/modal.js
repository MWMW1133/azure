// ================================
// modal.js - 안전버전 (중복 로드 방지)
// ================================
if (!window.__modalScriptLoaded__) {
  window.__modalScriptLoaded__ = true;

  console.debug("[modal.js] loaded ✅");

  // === 채팅 모달 열기/닫기 ===
  function openChatModal() {
    const el = document.getElementById('chatModal');
    if (el) el.classList.add('active');
  }

  function closeChatModal() {
    const el = document.getElementById('chatModal');
    if (el) el.classList.remove('active');
  }

  // === 말풍선 시간 포맷 ===
  function formatTime(date = new Date()) {
    return date.toLocaleTimeString('ko-KR', {
      hour: 'numeric',
      minute: 'numeric',
      hour12: true
    });
  }

  // ✅ 중복 선언 방지용 변수 선언
  if (!window.modalTime) {
    window.modalTime = formatTime();
  }

  // === 채팅방 생성 모달 ===
  function openCreateChatModal() {
    const el = document.getElementById('createChatModal');
    if (el) el.classList.add('active');
  }

  function closeCreateChatModal() {
    const el = document.getElementById('createChatModal');
    if (el) el.classList.remove('active');
  }

  // === 사이드바 DM 토글 ===
  function toggleDMList() {
    const dmList = document.getElementById("dmList");
    const dmSearchBox = document.getElementById("dmSearchBox");
    const icon = document.getElementById("dmToggleIcon");
    if (!dmList || !icon) return;

    const isClosed = dmList.style.display === "none";
    dmList.style.display = isClosed ? "block" : "none";
    if (dmSearchBox) dmSearchBox.style.display = isClosed ? "block" : "none";

    icon.classList.toggle("bi-caret-down-fill", isClosed);
    icon.classList.toggle("bi-caret-right-fill", !isClosed);
  }

  // === DM 검색 ===
  function filterDM(query) {
    query = query.toLowerCase();
    const items = document.querySelectorAll("#dmList li");
    items.forEach(li => {
      const match = li.textContent.toLowerCase().includes(query);
      li.style.display = match ? "block" : "none";
    });
  }

  // === 더미 데이터 ===
  const dummyProjects = ["프로젝트 1", "프로젝트 2", "프로젝트 3"];
  const dummyDMs = ["홍길동", "김철수", "이영희"];
  const dummyMessages = {
    "프로젝트 1": [
      { sender: "홍길동", text: "안녕하세요 👋", time: "오전 10:30", side: "left" },
      { sender: "나", text: "네 반가워요!", time: "오전 10:31", side: "right" }
    ],
    "프로젝트 2": [
      { sender: "김철수", text: "회의 언제하나요?", time: "오후 2:00", side: "left" }
    ],
    "프로젝트 3": [],
    "홍길동": [{ sender: "홍길동", text: "DM 테스트", time: "오전 9:10", side: "left" }],
    "김철수": [],
    "이영희": []
  };

  // === 채팅 목록 ===
  function renderGroupChats() {
    const list = document.getElementById("groupChatList");
    if (!list) return;
    list.innerHTML = "";
    dummyProjects.forEach(name => {
      const li = document.createElement("li");
      li.textContent = name;
      li.dataset.room = name;
      li.addEventListener("click", () => selectRoom(li, name));
      list.appendChild(li);
    });
  }

  function renderDMs() {
    const list = document.getElementById("dmList");
    if (!list) return;
    list.innerHTML = "";
    dummyDMs.forEach(name => {
      const li = document.createElement("li");
      li.textContent = name;
      li.dataset.room = name;
      li.addEventListener("click", () => selectRoom(li, name));
      list.appendChild(li);
    });
  }

  // === 메시지 렌더링 ===
  function renderMessages(roomName) {
    const container = document.getElementById("chatMessages");
    if (!container) return;
    container.innerHTML = "";
    const messages = dummyMessages[roomName] || [];

    messages.forEach(msg => {
      const div = document.createElement("div");
      div.className = `message ${msg.side}`;
      div.innerHTML = `
        ${msg.side === "left" ? `<img src="/images/profile1.png" class="avatar">` : ""}
        <div class="bubble">
            <div class="meta">
                <span class="sender">${msg.sender}</span>
                <span class="time">${msg.time}</span>
            </div>
            <p>${msg.text}</p>
        </div>
        ${msg.side === "right" ? `<img src="/images/my-cat.png" class="avatar">` : ""}
      `;
      container.appendChild(div);
    });
  }

  // === 방 선택 ===
  function selectRoom(clickedLi, roomName) {
    document.querySelectorAll("#groupChatList li, #dmList li")
      .forEach(li => li.classList.remove("active"));

    clickedLi.classList.add("active");
    const header = document.getElementById("chatHeaderTitle");
    if (header) header.textContent = roomName;
    renderMessages(roomName);
  }

  // === 채팅방 선택 옵션 ===
  function renderProjectSelect() {
    const select = document.getElementById("projectSelect");
    if (!select) return;
    select.innerHTML = "";
    dummyProjects.forEach(name => {
      const opt = document.createElement("option");
      opt.textContent = name;
      select.appendChild(opt);
    });
  }

  // === 초기화 ===
  document.addEventListener("DOMContentLoaded", () => {
    renderGroupChats();
    renderDMs();
    renderProjectSelect();

    if (dummyProjects.length > 0) {
      const first = dummyProjects[0];
      const header = document.getElementById("chatHeaderTitle");
      if (header) header.textContent = first;
      renderMessages(first);
      const firstLi = document.querySelector("#groupChatList li");
      if (firstLi) firstLi.classList.add("active");
    }
  });

} else {
  console.debug("[modal.js] already loaded — skipped ⚠️");
}
