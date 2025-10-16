// WS(STOMP) + REST 연동 클라이언트 (IME 안전 + 견고한 입력 탐색/재시도)
(function (w, d) {
  'use strict';

  const APP_CTX = w.APP_CTX || '';
  w.APP = w.APP || {};
  // ✅ 하드코딩 제거: 서버에서 목록 받아오므로 빈 객체 유지
  w.APP.channelMap = {};
  w.APP.useBackend = true;

  // (선택) DM 이름→상대 userId 매핑. 이벤트 detail.peerId가 오면 이 맵은 안 써도 됨.
  w.APP.dmMap = w.APP.dmMap || {};

  const currentUser = { id: (w.CURRENT_USER_ID || 1), name: '나' };
  const $ = (sel, el = d) => el.querySelector(sel);

  // ---- 입력 엘리먼트 견고한 탐색 (모달 내 우선 → 전체) ----
  function getChatInputEl() {
    const modal = d.getElementById('chatModal') || d;
    return (
      modal.querySelector('#chatTextInput') ||
      modal.querySelector('#chatInput') ||
      modal.querySelector('.chat-input input[type="text"]') ||
      d.querySelector('#chatTextInput') ||
      d.querySelector('#chatInput') ||
      d.querySelector('.chat-input input[type="text"]')
    );
  }

  // ---- 유저 이름 캐시 ----
  const userNameMap = {}; // { [userId]: name }

  // 현재 로그인 사용자/팀원 이름을 한 번만 가져와 캐시에 저장
  async function syncUserNamesOnce() {
    if (syncUserNamesOnce.__done) return; // 1회만
    try {
      // 1) 내 정보
      const meRes = await fetch(`${APP_CTX}/api/users/me`, { credentials: 'same-origin' });
      if (meRes.ok) {
        const me = await meRes.json();
        if (me?.id) {
          currentUser.id = Number(me.id);
          currentUser.name = me.name || currentUser.name || '나';
          userNameMap[currentUser.id] = currentUser.name;
          try { window.CURRENT_USER_ID = currentUser.id; } catch(_) {}
        }
      }
    } catch (_) {}

    try {
      // 2) 같은 조직 팀원 목록
      const r = await fetch(`${APP_CTX}/api/users`, { credentials: 'same-origin' });
      if (r.ok) {
        const arr = await r.json();
        arr.forEach(u => {
          const id = Number(u.id ?? u.userId ?? u.uid);
          const name = u.name ?? u.username ?? u.displayName ?? '';
          if (id && name) userNameMap[id] = name;
        });
      }
    } catch (_) {}

    syncUserNamesOnce.__done = true;
  }

  // === 하단 자동 스크롤 유틸 ===
  const $scroll = () => document.querySelector('#chatMessages');
  function scrollToBottom(force = false) {
    const el = $scroll();
    if (!el) return;
    const threshold = 80;
    const distanceFromBottom = el.scrollHeight - el.scrollTop - el.clientHeight;
    const nearBottom = distanceFromBottom <= threshold;
    if (force || nearBottom) el.scrollTop = el.scrollHeight;
  }

  // ---- 상태 ----
  let client = null;
  let subscription = null;
  let currentChannelId = null;
  let lastSelection = null; // {type, roomName, channelId?, peerId?}

  w.__chatDbg = w.__chatDbg || {};
  w.__chatDbg.getState = () => ({ connected: !!(client && client.connected), currentChannelId });

  // ---- 렌더 ----
  const escapeHTML = (s='') =>
    s.replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));

  function appendMsg({ text, authorId, createdAt }) {
    if (text == null || String(text).trim() === '') return; // 빈 메시지 무시
    const chatBox = $('#chatMessages');
    if (!chatBox) return;
    const side = (authorId === currentUser.id) ? 'right' : 'left';
    const div = d.createElement('div');
    div.className = `message ${side}`;
    const time = createdAt ? new Date(createdAt).toLocaleTimeString('ko-KR', {
      hour: 'numeric', minute: 'numeric', hour12: true
    }) : '';
    const rawName =
      (authorId === currentUser.id ? (currentUser.name || '') : (userNameMap[authorId] || ''));

    const nameHtml = rawName ? `<span class="sender">${escapeHTML(rawName)}</span>` : '';

    div.innerHTML = `
      ${side === 'left' ? `<img src="${APP_CTX}/images/profile1.png" class="avatar">` : ''}
      <div class="bubble">
        <div class="meta">
          ${nameHtml}
          <span class="time">${time}</span>
        </div>
        <p>${escapeHTML(text)}</p>
      </div>
      ${side === 'right' ? `<img src="${APP_CTX}/images/my-cat.png" class="avatar">` : ''}
    `;

    chatBox.appendChild(div);
    chatBox.scrollTop = chatBox.scrollHeight;
  }

  // ---- STOMP ----
  function ensureWs() {
    if (client && client.connected) return Promise.resolve();
    return new Promise((resolve) => {
      const Sock = new SockJS(APP_CTX + '/ws-chat');
      client = new StompJs.Client({
        webSocketFactory: () => Sock,
        reconnectDelay: 3000,
        debug: (m) => console.log('[stomp]', m)
      });
      client.onConnect = () => { console.log('[chat] STOMP connected'); syncUserNamesOnce().catch(() => {}); resolve(); };
      client.onStompError = (f) => console.error('[chat] STOMP error', f);
      client.onWebSocketError = (e) => console.error('[chat] WS error', e);
      client.activate();
      w.__chatDbg.client = client;
      window.stompClient = client; // (레거시 코드 호환)
    });
  }

  // ---- 메시지 로드(신·구 API 모두 지원) + ★쿠키 포함 ----
  async function loadMessages(channelId, limit = 50) {
    const tryNew = fetch(`${APP_CTX}/api/channels/${channelId}/messages?limit=${limit}`, {
      credentials: 'same-origin'
    }).then(r => r.ok ? r.json() : Promise.reject(r.status));

    const tryLegacy = () => fetch(`${APP_CTX}/api/messages/channels/${channelId}?page=0&size=${limit}`, {
      credentials: 'same-origin'
    }).then(r => r.ok ? r.json() : Promise.reject(r.status));

    try {
      return await tryNew;
    } catch (_e) {
      try { return await tryLegacy(); }
      catch (e2) {
        console.warn('[chat] loadMessages failed', _e, e2);
        return [];
      }
    }
  }

  async function enterChannel(channelId) {
    await syncUserNamesOnce();
    await ensureWs();

    if (subscription) { try { subscription.unsubscribe(); } catch(e){} subscription = null; }
    currentChannelId = Number(channelId);
    console.log('[chat] enterChannel ->', currentChannelId);

    const chatBox = $('#chatMessages');
    if (chatBox) chatBox.innerHTML = '';

    try {
      const listRaw = await loadMessages(currentChannelId, 100);

      const rows = Array.isArray(listRaw)
        ? listRaw
        : (listRaw?.content || listRaw?.items || listRaw?.data || []);

      const getTs = (m) => m.createdAt || m.created_at || m.ts || m.time || m.date;
      rows
        .slice()
        .sort((a, b) => new Date(getTs(a)) - new Date(getTs(b)))
        .forEach(m => appendMsg({
          text: m.body ?? m.text ?? m.message ?? '',
          authorId: m.authorId ?? m.senderId ?? m.userId,
          createdAt: getTs(m)
        }));

      requestAnimationFrame(() => scrollToBottom(true));
    } catch (e) {
      console.error(e);
    }

    subscription = client.subscribe(`/topic/chat/${currentChannelId}`, (frame) => {
      let data; try { data = JSON.parse(frame.body); } catch(_) { return; }
      const text = (data.body ?? data.text ?? data.message ?? '').toString();
      if (text.trim() === '') return; // 내용 없으면 렌더 X

      appendMsg({
        text,
        authorId: Number(data.authorId ?? data.senderId ?? data.userId),
        createdAt: (data.createdAt || data.created_at || data.ts || data.time || data.date)
      });
      requestAnimationFrame(() => scrollToBottom(false));
    });
  }

  // ---- DM 전용: 채널ID 확보 후 진입 (★ 쿠키 포함) ----
  async function getOrCreateDmChannelId(peerId) {
    const url =  `${APP_CTX}/api/channels/dm/${peerId}/channel`;
    const res = await fetch(url, {
      credentials: 'same-origin',
      headers: { 'Accept': 'application/json' }
    });
    if (res.status === 401) { console.warn('[chat] DM 401 Unauthorized'); alert('로그인이 필요합니다.'); return null; }
    if (!res.ok) { console.warn('[chat] DM channel fetch failed', res.status); return null; }
    const { channelId } = await res.json();
    return channelId || null;
  }

  async function enterDMByPeer(peerId, nameForLog = '') {
    const chId = await getOrCreateDmChannelId(peerId);
    if (!chId) { console.warn('⚠️ DM 채널 ID 없음', { peerId, nameForLog }); safeReset(); return; }
    console.log('[chat] enterDM ->', chId, nameForLog);
    await enterChannel(chId);
  }

  // ---- 전송 (빈값일 경우 rAF + 120ms 재시도) ----
  function readInputValue() {
    const el = getChatInputEl();
    return { el, value: (el?.value ?? '').trim() };
  }

  function send(textOverride) {
    const { el, value } = readInputValue();
    const firstTry = (textOverride ?? value);
    if (!el) { console.warn('[chat] input not found'); return; }

    if (!firstTry) {
      requestAnimationFrame(() => {
        setTimeout(async () => {
          const retryVal = (getChatInputEl()?.value ?? '').trim();
          if (!retryVal) { console.warn('[chat] empty text even after retry'); return; }
          await _sendNow(retryVal);
        }, 120);
      });
      return;
    }
    _sendNow(firstTry);
  }

  // === 전송: 서버가 번역하도록 플래그만 전달 ===
  async function _sendNow(text) {
    if (!client || !client.connected) { console.warn('[chat] not connected'); return; }
    if (!currentChannelId) { console.warn('[chat] no channel selected'); return; }

    const root = document.getElementById('chatModal') || document;

    const translateToggle = root.querySelector('#mt-enable, #tg-translate, #translateToggle, #translateSwitch');
    const translateEnabled = !!(translateToggle && translateToggle.checked);

    const rawTarget =
      root.querySelector('#mt-target, #sel-target, #translateLang, #translateSelect')?.value || 'en';
    const targetLang = normalizeTarget(rawTarget);

    const payload = {
      channelId: currentChannelId,
      authorId: currentUser.id,
      body: text
    };

    // ✅ 토글이 켜져 있으면, 대상 언어가 무엇이든 항상 전달
    if (translateEnabled) {
      payload.translateEnabled = true;
      payload.targetLang = targetLang;
    } else {
      console.log('[chat] 번역 비활성 상태 → 원문 그대로 전송');
    }

    console.log('[chat] publish ->', payload);
    client.publish({
      destination: `/app/chat/${currentChannelId}/send`,
      body: JSON.stringify(payload)
    });

    const el = getChatInputEl();
    if (el) el.value = '';
    requestAnimationFrame(() => scrollToBottom(true));
  }

  // ---- 방 선택 이벤트 ----
  w.addEventListener('chat:room-selected', async (ev) => {
    const { type, roomName, channelId, peerId } = ev.detail || {};
    console.log('[chat] room-selected', ev.detail);

    lastSelection = { type, roomName, channelId, peerId };
    try { localStorage.setItem('chat:last', JSON.stringify(lastSelection)); } catch(e) {}

    if (type === 'DM') {
      const pid = peerId || findPeerIdFromDom(roomName);
      if (!pid) { console.warn('DM 클릭인데 peerId 없음:', roomName); safeReset(); return; }
      await enterDMByPeer(Number(pid), roomName); return;
    }

    const mappedGroupId = channelId || (w.APP.channelMap?.[roomName]); // channelMap은 비어있음(하위호환)
    if (mappedGroupId) { await enterChannel(Number(mappedGroupId)); return; }

    const pid = peerId || w.APP.dmMap?.[roomName] || findPeerIdFromDom(roomName);
    if (pid) { await enterDMByPeer(Number(pid), roomName); return; }

    console.warn('채널ID/peerId 없음 → 동작 보류:', roomName, channelId);
    safeReset();
  });

  // === 헬퍼들 ===
  function findPeerIdFromDom(name){
    const dm = Array.from(document.querySelectorAll('.dm-item'));
    const node = dm.find(li => li.textContent.trim() === name);
    if (!node) return null;
    return Number(node.dataset.userId || node.dataset.peerId || NaN);
  }
  function safeReset(){
    if (subscription) { try { subscription.unsubscribe(); } catch(e){} }
    subscription = null;
    currentChannelId = null;
  }

  // ---- 버튼/엔터(IME 안전) ----
  d.addEventListener('click', (e) => {
    const t = e.target;
    if (t && (t.id === 'btnChatSend' || t.closest('#btnChatSend'))) {
      e.preventDefault();
      send();
    }
  });

  function bindEnterOnce() {
    const el = getChatInputEl();
    if (!el) return;
    el.addEventListener('keydown', (e) => {
      if (e.isComposing) return;
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        send();
      }
    }, { once: true });
  }
  const obs = new MutationObserver(() => {
    const el = getChatInputEl();
    if (el && !el.__enterBound) { bindEnterOnce(); el.__enterBound = true; }
  });
  obs.observe(d.body, { childList: true, subtree: true });

  // 기존 onclick="sendMessage()" 호환
  w.sendMessage = function () { send(); };
  w.sendmessage = w.sendMessage;

  // 수동 진입 디버깅용
  w.__enterChannel = enterChannel;
  w.__enterDMByPeer = enterDMByPeer;

  // ★ 모달 재오픈 시 히스토리 복원
  w.addEventListener('chat:reopen', async () => {
    try {
      if (currentChannelId) { await enterChannel(currentChannelId); return; }
      let saved = lastSelection;
      if (!saved) {
        try { saved = JSON.parse(localStorage.getItem('chat:last') || 'null'); } catch(e) {}
      }
      if (saved) {
        const { type, roomName, channelId, peerId } = saved;
        if (type === 'DM' && (peerId || roomName)) {
          const pid = Number(peerId || findPeerIdFromDom(roomName));
          if (pid) { await enterDMByPeer(pid, roomName || ''); return; }
        }
        if (channelId) { await enterChannel(Number(channelId)); return; }
      }
      const active = d.querySelector('#groupChatList li.active, #dmList li.active');
      if (active) {
        const detail = active.dataset.peerId
          ? { type:'DM', roomName: active.dataset.room, peerId:Number(active.dataset.peerId) }
          : { type:'CHANNEL', roomName: active.dataset.room, channelId:Number(active.dataset.channelId) };
        w.dispatchEvent(new CustomEvent('chat:room-selected', { detail }));
      }
    } catch (e) {
      console.warn('[chat] reopen restore failed', e);
    }
  });

})(window, document);

/* ============================
   [번역] 라벨 → 언어코드 정규화
   ============================ */
function normalizeTarget(val) {
  if (!val) return 'en';
  const s = String(val).trim().toLowerCase();
  if (['en','english','영어'].includes(s)) return 'en';
  if (['ko','korean','한국어','한글'].includes(s)) return 'ko';
  if (['ja','japanese','일본어'].includes(s)) return 'ja';
  if (['zh','chinese','중국어','zh-cn','cn'].includes(s)) return 'zh';
  return 'en';
}

/* ============================
   프로젝트 방 목록 가져와서 렌더
   ============================ */
async function loadProjectRooms() {
  const ul = document.querySelector('#groupChatList');
  if (!ul) return;

  // 로딩 표시
  ul.innerHTML = '<li class="text-muted" style="padding:8px 12px;">불러오는 중…</li>';

  try {
    // window.CURRENT_USER_ID 가 아직 없을 수도 있으니 방어
    const me = Number(window.CURRENT_USER_ID || 0) || 1;
    const res = await fetch(`${(window.APP_CTX||'')}/api/chat/rooms/projects?userId=${encodeURIComponent(me)}`, {
      credentials: 'same-origin',
      headers: { 'Accept': 'application/json' }
    });
    if (!res.ok) throw new Error('HTTP ' + res.status);

    /** 기대 형식: [{projectId, name, channelId}] */
    const rows = await res.json();

    if (!Array.isArray(rows) || rows.length === 0) {
      ul.innerHTML = '<li class="text-muted" style="padding:8px 12px;">프로젝트가 없습니다</li>';
      return;
    }

    ul.innerHTML = '';
    rows.forEach(r => {
      const li = document.createElement('li');
      li.className = 'room-item';
      li.textContent = r.name;
      li.dataset.room = r.name;
      if (r.channelId) li.dataset.channelId = r.channelId; // 이미 채널 있으면 바로 사용
      li.dataset.projectId = r.projectId;                   // 없으면 클릭 시 보장(ensure)

      li.addEventListener('click', async () => {
        // UI 활성화 토글
        ul.querySelectorAll('li').forEach(x => x.classList.remove('active'));
        li.classList.add('active');

        // 1) 채널ID가 있으면 즉시 입장
        const ch = li.dataset.channelId && Number(li.dataset.channelId);
        if (ch) {
          window.dispatchEvent(new CustomEvent('chat:room-selected', {
            detail: { type: 'CHANNEL', roomName: r.name, channelId: ch }
          }));
          return;
        }

        // 2) 없으면 서버에 보장(ensure) 요청 후 입장
        try {
          const res2 = await fetch(
            `${(window.APP_CTX||'')}/api/chat/rooms/projects/${r.projectId}/ensure-channel?me=${encodeURIComponent(me)}`,
            { method: 'POST', credentials: 'same-origin' }
          );
          if (!res2.ok) throw new Error('HTTP ' + res2.status);
          const channelId = await res2.json();
          li.dataset.channelId = channelId;

          window.dispatchEvent(new CustomEvent('chat:room-selected', {
            detail: { type: 'CHANNEL', roomName: r.name, channelId }
          }));
        } catch (e) {
          console.warn('ensure-channel failed', e);
          alert('채팅 채널을 만들거나 찾는 데 실패했습니다.');
        }
      });

      ul.appendChild(li);
    });

    const q = document.getElementById('groupSearchBox')?.value || '';
    if (q) window.filterGroup(q);
    
  } catch (e) {
    console.error('loadProjectRooms failed', e);
    ul.innerHTML = '<li class="text-danger" style="padding:8px 12px;">목록을 불러오지 못했습니다</li>';
  }
}
