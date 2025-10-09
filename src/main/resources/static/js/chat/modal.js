// modal.js — safe, single-load, no global collisions
(function (w, d) {
  'use strict';

  // ★ 파일 중복 로드 가드
  if (w.__modalLoaded) return;
  w.__modalLoaded = true;

  // -------------------------------------------------
  // Utils
  // -------------------------------------------------
  function formatTime(date = new Date()) {
    return date.toLocaleTimeString('ko-KR', {
      hour: 'numeric',
      minute: 'numeric',
      hour12: true
    });
  }

  // 필요하면 외부에서 쓸 수 있도록 네임스페이스로 제공
  w.APP = w.APP || {};
  w.APP.modal = w.APP.modal || {};
  w.APP.modal.formatTime = formatTime;

  // -------------------------------------------------
  // 공개 함수 (HTML onclick 등에서 호출 가능하도록 window에 바인딩)
  // -------------------------------------------------
  w.openChatModal = function openChatModal() {
    const el = d.getElementById('chatModal');
    if (el) el.classList.add('active');
  };

  w.closeChatModal = function closeChatModal() {
    const el = d.getElementById('chatModal');
    if (el) el.classList.remove('active');
  };

  w.openCreateChatModal = function openCreateChatModal() {
    const el = d.getElementById('createChatModal');
    if (el) el.classList.add('active');
  };

  w.closeCreateChatModal = function closeCreateChatModal() {
    const el = d.getElementById('createChatModal');
    if (el) el.classList.remove('active');
  };

  w.toggleDMList = function toggleDMList() {
    const dmList = d.getElementById('dmList');
    const dmSearchBox = d.getElementById('dmSearchBox');
    const icon = d.getElementById('dmToggleIcon');
    if (!dmList || !dmSearchBox || !icon) return;

    const opened = dmList.style.display !== 'none';
    dmList.style.display = opened ? 'none' : 'block';
    dmSearchBox.style.display = opened ? 'none' : 'block';
    icon.classList.toggle('bi-caret-right-fill', opened);
    icon.classList.toggle('bi-caret-down-fill', !opened);
  };

  w.filterDM = function filterDM(query) {
    query = (query || '').toLowerCase();
    d.querySelectorAll('#dmList li').forEach(li => {
      li.style.display = li.textContent.toLowerCase().includes(query) ? 'block' : 'none';
    });
  };

  // -------------------------------------------------
  // 더미 데이터 (필요 시 API 연동으로 교체)
  // -------------------------------------------------
  const dummyProjects = ['프로젝트 1', '프로젝트 2', '프로젝트 3'];
  const dummyDMs = ['홍길동', '김철수', '이영희'];
  const dummyMessages = {
    '프로젝트 1': [
      { sender: '홍길동', text: '안녕하세요 👋', time: '오전 10:30', side: 'left' },
      { sender: '나', text: '네 반가워요!',   time: '오전 10:31', side: 'right' }
    ],
    '프로젝트 2': [
      { sender: '김철수', text: '회의 언제하나요?', time: '오후 2:00', side: 'left' }
    ],
    '프로젝트 3': [],
    '홍길동': [{ sender: '홍길동', text: 'DM 테스트', time: '오전 9:10', side: 'left' }],
    '김철수': [],
    '이영희': []
  };

  // -------------------------------------------------
  // 내부 렌더 함수들 (전역에 안 흘러가게 지역으로)
  // -------------------------------------------------
  function renderGroupChats() {
    const list = d.getElementById('groupChatList');
    if (!list) return;
    list.innerHTML = '';
    dummyProjects.forEach(name => {
      const li = d.createElement('li');
      li.textContent = name;
      li.dataset.room = name;
      li.dataset.channelId = (w.APP?.channelMap?.[name] ?? ''); // ← 채널 ID 주입
      li.addEventListener('click', () => selectRoom(li, name));
      list.appendChild(li);
    });
  }

  function renderDMs() {
    const list = d.getElementById('dmList');
    if (!list) return;
    list.innerHTML = '';
    dummyDMs.forEach(name => {
      const li = d.createElement('li');
      li.textContent = name;
      li.dataset.room = name;
      li.dataset.channelId = (w.APP?.channelMap?.[name] ?? '');
      li.addEventListener('click', () => selectRoom(li, name));
      list.appendChild(li);
    });
  }

  function renderMessages(roomName) {
    const container = d.getElementById('chatMessages');
    if (!container) return;
    container.innerHTML = '';
    const messages = dummyMessages[roomName] || [];
    messages.forEach(msg => {
      const div = d.createElement('div');
      div.className = `message ${msg.side}`;
      div.innerHTML = `
        ${msg.side === 'left' ? `<img src="/images/profile1.png" class="avatar">` : ''}
        <div class="bubble">
          <div class="meta">
            <span class="sender">${msg.sender}</span>
            <span class="time">${msg.time}</span>
          </div>
          <p>${msg.text}</p>
        </div>
        ${msg.side === 'right' ? `<img src="/images/my-cat.png" class="avatar">` : ''}
      `;
      container.appendChild(div);
    });
  }

  function selectRoom(clickedLi, roomName) {
    d.querySelectorAll('#groupChatList li, #dmList li').forEach(li => li.classList.remove('active'));
    if (clickedLi) clickedLi.classList.add('active');
    const header = d.getElementById('chatHeaderTitle');
    if (header) header.textContent = roomName;
      // 백엔드 모드면 client.js에게 방 변경 알림
      const channelId = clickedLi?.dataset?.channelId || null;
      if (w.APP && w.APP.useBackend) {
        w.dispatchEvent(new CustomEvent('chat:room-selected', {
          detail: { roomName, channelId }
        }));
      } else {
      // (개발 초기 더미 렌더 유지용)
      renderMessages(roomName);
      }
    }
  function renderProjectSelect() {
    const select = d.getElementById('projectSelect');
    if (!select) return;
    select.innerHTML = '';
    dummyProjects.forEach(name => {
      const opt = d.createElement('option');
      opt.textContent = name;
      select.appendChild(opt);
    });
  }

  // -------------------------------------------------
  // 초기화 (중복 실행 가드)
  // -------------------------------------------------
  function initOnce() {
    if (w.__modalInit) return;
    w.__modalInit = true;

    renderGroupChats();
    renderDMs();
    renderProjectSelect();

    // 기본 첫 방
    if (dummyProjects.length > 0) {
      const first = dummyProjects[0];
      const header = d.getElementById('chatHeaderTitle');
      if (header) header.textContent = first;
      renderMessages(first);
      const firstLi = d.querySelector('#groupChatList li');
      if (firstLi) firstLi.classList.add('active');
    }
  }

  if (d.readyState === 'loading') {
    d.addEventListener('DOMContentLoaded', initOnce, { once: true });
  } else {
    initOnce();
  }

})(window, document);
