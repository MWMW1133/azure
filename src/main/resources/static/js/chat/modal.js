
// modal.js — safe, single-load, no global collisions
(function (w, d) {
  'use strict';

  // 중복 로드 가드
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

  w.APP = w.APP || {};
  w.APP.modal = w.APP.modal || {};
  w.APP.modal.formatTime = formatTime;

  const APP_CTX = w.APP_CTX || '';
  let __dmRenderedOnce = false;   // DM 목록 1회 캐시
  let __dmLock = false;           // DM 진입 중복 방지

  // 로그인 유저 id 동기화 (백업 경로)
  async function syncCurrentUserId() {
    try {
      const current = Number(window.CURRENT_USER_ID || 0);
      if (current > 0) return;
      const r = await fetch(`${APP_CTX}/api/users/me`, {
        credentials: 'same-origin',
        headers: { 'Accept': 'application/json' }
      });
      if (!r.ok) return;
      const me = await r.json();
      if (me && Number(me.id) > 0) {
        w.CURRENT_USER_ID = Number(me.id);
        // 다음 렌더 때 최신 반영
        __dmRenderedOnce = false;
      }
    } catch (_) {}
  }

  // -------------------------------------------------
  // 공개 함수 (HTML onclick 등에서 호출 가능)
  // -------------------------------------------------
  w.openChatModal = function openChatModal() {
    const el = d.getElementById('chatModal');
    if (el) {
      el.classList.add('active');
      // 내 id 동기화 후 DM 목록 로드
      syncCurrentUserId().then(() => {
        renderDMsFromApi();
        renderGroupChats(); // 프로젝트 목록도 최신으로
      });
      // 재오픈 시 현재/마지막 채널 복원
      w.dispatchEvent(new CustomEvent('chat:reopen'));
    }
  };

  w.closeChatModal = function closeChatModal() {
    const el = d.getElementById('chatModal');
    if (el) {
      el.classList.remove('active');
      // 계정 전환/새 로드를 대비
      __dmRenderedOnce = false;
    }
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
  // 개발용 더미(백엔드 미사용 시만 사용)
  // -------------------------------------------------
  const dummyProjects = ['프로젝트 1', '프로젝트 2', '프로젝트 3'];
  const dummyMessages = {
    '프로젝트 1': [
      { sender: '홍길동', text: '안녕하세요 👋', time: '오전 10:30', side: 'left' },
      { sender: '나', text: '네 반가워요!',   time: '오전 10:31', side: 'right' }
    ],
    '프로젝트 2': [
      { sender: '김철수', text: '회의 언제하나요?', time: '오후 2:00', side: 'left' }
    ],
    '프로젝트 3': []
  };

  // -------------------------------------------------
  // 그룹 채팅(프로젝트) 목록
  // -------------------------------------------------
  async function renderProjectsFromApi() {
    const list = d.getElementById('groupChatList');
    if (!list) return;

    list.innerHTML = '<li class="text-muted px-2">불러오는 중…</li>';

    // 서버가 "내가 접근 가능한 프로젝트 채널들"만 돌려준다고 가정
    // 예: [{ id: 1, name: "프로젝트 1" }, ...]
    // (아래 scope/type 파라미터는 실제 서버 구현에 맞춰 한쪽으로 고정해도 됨)
    const url = `${APP_CTX}/api/channels?scope=PROJECT`;
    try {
      const r = await fetch(url, {
        credentials: 'same-origin',
        headers: { 'Accept': 'application/json' }
      });
      if (!r.ok) throw new Error('HTTP ' + r.status);
      const channels = await r.json();

      list.innerHTML = '';
      (channels || []).forEach(ch => {
        const li = d.createElement('li');
        li.textContent = ch.name || ('채널 #' + ch.id);
        li.dataset.room = ch.name || '';
        li.dataset.channelId = String(ch.id);
        li.addEventListener('click', () => selectRoom(li, ch.name || ''));
        list.appendChild(li);
      });

      // 첫 번째 항목 선택 기본값
      const firstLi = list.querySelector('li');
      if (firstLi) firstLi.classList.add('active');
      const header = d.getElementById('chatHeaderTitle');
      if (header && channels && channels.length > 0) {
        header.textContent = channels[0].name || '';
      }
    } catch (err) {
      console.error('[modal] 프로젝트 채널 목록 실패:', err);
      list.innerHTML = '<li class="text-muted px-2">프로젝트 채널을 불러오지 못했습니다</li>';
    }
  }

  function renderProjectsDummy() {
    const list = d.getElementById('groupChatList');
    if (!list) return;
    list.innerHTML = '';
    dummyProjects.forEach(name => {
      const li = d.createElement('li');
      li.textContent = name;
      li.dataset.room = name;
      li.dataset.channelId = (w.APP?.channelMap?.[name] ?? '');
      li.addEventListener('click', () => selectRoom(li, name));
      list.appendChild(li);
    });

    // 기본 첫 방
    const first = dummyProjects[0];
    const header = d.getElementById('chatHeaderTitle');
    if (header) header.textContent = first;
    const firstLi = d.querySelector('#groupChatList li');
    if (firstLi) firstLi.classList.add('active');
    if (!(w.APP && w.APP.useBackend)) renderMessages(first);
  }

  function renderGroupChats() {
    if (w.APP && w.APP.useBackend) {
      renderProjectsFromApi();
    } else {
      renderProjectsDummy();
    }
  }

  // -------------------------------------------------
  // DM 목록
  // -------------------------------------------------
  async function renderDMsFromApi() {
    const list = d.getElementById('dmList');
    if (!list) return;
    if (__dmRenderedOnce) return;

    await syncCurrentUserId();

    fetch(`${APP_CTX}/api/users`, {
      credentials: 'same-origin',
      headers: { 'Accept': 'application/json' }
    })
      .then(r => {
        if (!r.ok) throw new Error('HTTP ' + r.status);
        return r.json();
      })
      .then(usersRaw => {
        const meId = Number(w.CURRENT_USER_ID || 0);
        const users = usersRaw.map(u => ({
          id: Number(u.id ?? u.userId ?? u.uid),
          name: u.name ?? u.username ?? u.displayName ?? '',
          avatarUrl: u.avatarUrl ?? u.avatar ?? null,
        }))
        .filter(u => u.id && u.name && u.id !== meId);

        list.innerHTML = '';
        users.forEach(u => {
          const li = d.createElement('li');
          li.className = 'chat-list__item dm-item';
          li.dataset.room = u.name;
          li.dataset.peerId = String(u.id);
          li.dataset.name = u.name;
          li.innerHTML = `
            <span class="avatar" style="background-image:url('${u.avatarUrl || (APP_CTX + '/images/profile1.png')}')"></span>
            <span class="name">${u.name}</span>
          `;
          li.addEventListener('click', () => selectRoom(li, u.name));
          list.appendChild(li);
        });

        __dmRenderedOnce = true;
      })
      .catch(err => {
        console.error('[modal] /api/users 실패:', err);
        list.innerHTML = '<li class="text-muted px-2">팀원을 불러오지 못했습니다</li>';
      });
  }

  // -------------------------------------------------
  // 메시지(더미) 렌더 — 백엔드 미사용일 때만
  // -------------------------------------------------
  function renderMessages(roomName) {
    const container = d.getElementById('chatMessages');
    if (!container) return;
    container.innerHTML = '';
    const messages = dummyMessages[roomName] || [];
    messages.forEach(msg => {
      const div = d.createElement('div');
      div.className = `message ${msg.side}`;
      div.innerHTML = `
        ${msg.side === 'left' ? `<img src="${APP_CTX}/images/profile1.png" class="avatar">` : ''}
        <div class="bubble">
          <div class="meta">
            <span class="sender">${msg.sender}</span>
            <span class="time">${msg.time}</span>
          </div>
          <p>${msg.text}</p>
        </div>
        ${msg.side === 'right' ? `<img src="${APP_CTX}/images/my-cat.png" class="avatar">` : ''}
      `;
      container.appendChild(div);
    });
  }

  // ---------- DM 전환: 서버에서 반드시 channelId를 확보 ----------
  async function enterDm(clickedLi, roomName, peerId) {
    await syncCurrentUserId();
    const me = Number(w.CURRENT_USER_ID || 0);
    if (Number(peerId) === me) {
      console.warn('[DM] self-block', { peerId, me, name: roomName });
      alert('자기 자신에게는 DM을 보낼 수 없습니다.');
      return;
    }
    if (__dmLock) return;
    __dmLock = true;

    const url = `${APP_CTX}/api/channels/dm/${peerId}/channel`;
    const opts = {
      credentials: 'same-origin',
      headers: { 'Accept': 'application/json' }
    };

    try {
      // 1) GET 시도 → 필요시 POST
      let r = await fetch(url, { ...opts, method: 'GET' });
      if (!r.ok && (r.status === 405 || r.status === 404 || r.status === 401)) {
        r = await fetch(url, { ...opts, method: 'POST' });
      }

      if (!r.ok) {
        if (r.status === 401) {
          alert('로그인이 필요합니다.');
        } else if (r.status === 404) {
          alert('대상 사용자를 찾을 수 없습니다.');
        } else if (r.status === 400) {
          try {
            const t = await r.text();
            const data = t ? JSON.parse(t) : null;
            alert(data?.message || data?.error || '요청이 올바르지 않습니다.');
          } catch {
            alert('요청이 올바르지 않습니다.');
          }
        } else {
          let msg = 'DM 채널 생성/조회에 실패했습니다. (' + r.status + ')';
          try {
            const t = await r.text();
            if (t) {
              const j = JSON.parse(t);
              msg = j.message || j.error || msg;
            }
          } catch {}
          alert(msg);
        }
        return;
      }

      const data = await r.json();
      const channelId =
        data?.channelId ?? data?.data?.channelId ?? data?.result?.channelId;

      if (!channelId) {
        console.warn('[DM] channelId가 응답에 없음:', data);
        alert('DM 채널 정보를 가져오지 못했습니다.');
        return;
      }

      // 선택 표시/헤더 갱신
      d.querySelectorAll('#groupChatList li, #dmList li').forEach(li => li.classList.remove('active'));
      if (clickedLi) clickedLi.classList.add('active');
      const header = d.getElementById('chatHeaderTitle');
      if (header) header.textContent = roomName;

      // client.js에게 이 채널로 들어가라고 알림
      w.dispatchEvent(new CustomEvent('chat:room-selected', {
        detail: { type: 'CHANNEL', roomName, channelId: Number(channelId) }
      }));
    } catch (err) {
      console.error('[DM] channel fetch failed:', err);
      alert('DM 채널 생성/조회 중 오류가 발생했습니다.');
    } finally {
      __dmLock = false;
    }
  }

  // 방 선택: 그룹채팅(채널ID) 혹은 DM(peerId→channelId)
  function selectRoom(clickedLi, roomName) {
    const channelId = clickedLi?.dataset?.channelId || null;
    const peerId    = clickedLi?.dataset?.peerId    || null;

    if (w.APP && w.APP.useBackend) {
      if (peerId) {
        enterDm(clickedLi, roomName, Number(peerId));
        return;
      }
      d.querySelectorAll('#groupChatList li, #dmList li').forEach(li => li.classList.remove('active'));
      if (clickedLi) clickedLi.classList.add('active');
      const header = d.getElementById('chatHeaderTitle');
      if (header) header.textContent = roomName;

      w.dispatchEvent(new CustomEvent('chat:room-selected', {
        detail: { type: 'CHANNEL', roomName, channelId: Number(channelId) || null }
      }));
      return;
    }

    // 개발용 더미
    d.querySelectorAll('#groupChatList li, #dmList li').forEach(li => li.classList.remove('active'));
    if (clickedLi) clickedLi.classList.add('active');
    const header = d.getElementById('chatHeaderTitle');
    if (header) header.textContent = roomName;
    renderMessages(roomName);
  }

  // === 채팅방 선택 옵션 ===
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
  // 초기화
  // -------------------------------------------------
  function initOnce() {
    if (w.__modalInit) return;
    w.__modalInit = true;

    renderGroupChats();
    renderProjectSelect();


    // (백엔드 사용 시엔 renderProjectsFromApi 내부에서 첫 항목을 active 처리)
    if (!w.APP?.useBackend && dummyProjects.length > 0) {
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