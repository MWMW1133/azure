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
      const current = Number(w.CURRENT_USER_ID || 0);
      if (current > 0) return;
      const r = await fetch(`${APP_CTX}/api/users/me`, {
        credentials: 'same-origin',
        headers: { 'Accept': 'application/json' }
      });
      if (!r.ok) return;
      const me = await r.json();
      if (me && Number(me.id) > 0) {
        w.CURRENT_USER_ID = Number(me.id);
        __dmRenderedOnce = false; // 다음 렌더 때 최신 반영
      }
    } catch (_) {}
  }

  // -------------------------------------------------
  // 공개 함수 (HTML onclick 등에서 호출)
  // -------------------------------------------------
  w.openChatModal = function openChatModal() {
    const el = d.getElementById('chatModal');
    if (!el) return;

    el.classList.add('active');

    // 내 id 동기화 → DM/프로젝트 목록 로드 → UI 보장 → 마지막 채널 복원
    syncCurrentUserId()
      .then(() => {
        renderDMsFromApi();
        if (typeof w.loadProjectRooms === 'function') {
          return w.loadProjectRooms();
        }
      })
      .then(() => {
        ensureGroupUI();                    // ✅ 그룹 토글/검색 UI 보장
        w.dispatchEvent(new CustomEvent('chat:reopen'));
      });
  };

  w.closeChatModal = function closeChatModal() {
    const el = d.getElementById('chatModal');
    if (el) {
      el.classList.remove('active');
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

  // -------------------------------------------------
  // DM 토글/검색
  // -------------------------------------------------
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
    const q = (query || '').toLowerCase();
    d.querySelectorAll('#dmList li').forEach(li => {
      li.style.display = li.textContent.toLowerCase().includes(q) ? 'block' : 'none';
    });
  };

  // -------------------------------------------------
  // DM 목록 (백엔드)
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
        })).filter(u => u.id && u.name && u.id !== meId);

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

  // ---------- DM 전환: 서버에서 반드시 channelId를 확보 ----------
  async function enterDm(clickedLi, roomName, peerId) {
    await syncCurrentUserId();
    const me = Number(w.CURRENT_USER_ID || 0);
    if (Number(peerId) === me) {
      alert('자기 자신에게는 DM을 보낼 수 없습니다.');
      return;
    }
    if (__dmLock) return;
    __dmLock = true;

    const url = `${APP_CTX}/api/channels/dm/${peerId}/channel`;
    const opts = { credentials: 'same-origin', headers: { 'Accept': 'application/json' } };

    try {
      let r = await fetch(url, { ...opts, method: 'GET' });
      if (!r.ok && (r.status === 405 || r.status === 404 || r.status === 401)) {
        r = await fetch(url, { ...opts, method: 'POST' });
      }
      if (!r.ok) {
        if (r.status === 401) alert('로그인이 필요합니다.');
        else if (r.status === 404) alert('대상 사용자를 찾을 수 없습니다.');
        else alert('DM 채널 생성/조회에 실패했습니다. (' + r.status + ')');
        return;
      }

      const data = await r.json();
      const channelId = data?.channelId ?? data?.data?.channelId ?? data?.result?.channelId;
      if (!channelId) { alert('DM 채널 정보를 가져오지 못했습니다.'); return; }

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

    // 백엔드 사용
    if (w.APP && w.APP.useBackend) {
      if (peerId) { enterDm(clickedLi, roomName, Number(peerId)); return; }
      d.querySelectorAll('#groupChatList li, #dmList li').forEach(li => li.classList.remove('active'));
      if (clickedLi) clickedLi.classList.add('active');
      const header = d.getElementById('chatHeaderTitle');
      if (header) header.textContent = roomName;

      w.dispatchEvent(new CustomEvent('chat:room-selected', {
        detail: { type: 'CHANNEL', roomName, channelId: Number(channelId) || null }
      }));
    }
  }

  // -------------------------------------------------
  // 그룹 섹션: 토글/검색 UI 강제 주입 & 바인딩
  // -------------------------------------------------
  function ensureGroupUI() {
    const list = d.getElementById('groupChatList');
    if (!list) return;

    // 헤더(토글 버튼) 없으면 생성
    if (!d.getElementById('btnGroupToggle')) {
      const header = d.createElement('div');
      header.className = 'd-flex align-items-center justify-content-between';
      header.innerHTML = `
        <button type="button" class="btn btn-sm btn-link p-0 d-flex align-items-center" id="btnGroupToggle">
          <i id="groupToggleIcon" class="bi bi-caret-down-fill me-1"></i> 그룹 채팅
        </button>
      `;
      list.parentElement.insertBefore(header, list);
      d.getElementById('btnGroupToggle').addEventListener('click', toggleGroupList);
    }

    // 검색 박스 없으면 생성
    if (!d.getElementById('groupSearchBox')) {
      const box = d.createElement('div');
      box.id = 'groupSearchBox';
      box.className = 'mt-2';
      box.innerHTML = `
        <input type="text" id="groupSearchInput"
               class="form-control form-control-sm"
               placeholder="프로젝트 검색..." />
      `;
      list.parentElement.insertBefore(box, list);

      const input = d.getElementById('groupSearchInput');
      input.addEventListener('input', (e) => filterGroup(e.target.value));
    }
  }

  // === 그룹 리스트 접기/펼치기 ===
  window.toggleGroupList = function () {
  const list = document.getElementById('groupChatList');
  const box  = document.getElementById('groupSearchBox');
  const icon = document.getElementById('groupToggleIcon');
  if (!list || !box || !icon) return;

  const opened = list.style.display !== 'none';
  list.style.display = opened ? 'none' : 'block';
  box.style.display  = opened ? 'none' : 'block';
  icon.classList.toggle('bi-caret-right-fill', opened);
  icon.classList.toggle('bi-caret-down-fill', !opened);
};

  // === 그룹 리스트 검색 ===
  function filterGroup(query) {
    const q = (query || '').toLowerCase();
    d.querySelectorAll('#groupChatList li').forEach(li => {
      const text = li.textContent.toLowerCase();
      li.style.display = text.includes(q) ? 'block' : 'none';
    });
  }
  w.filterGroup = filterGroup;

  // 모달 재오픈 시 검색 상태 유지 + UI 보장
  w.addEventListener('chat:reopen', () => {
    ensureGroupUI();
    const q = (d.getElementById('groupSearchInput')?.value || '').trim();
    if (q) filterGroup(q);
  });

})(window, document);
