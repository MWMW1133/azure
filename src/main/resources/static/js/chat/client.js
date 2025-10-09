// /static/js/chat/client.js
// WS(STOMP) + REST 연동 클라이언트 (IME 안전 + 견고한 입력 탐색/재시도)

(function (w, d) {
  'use strict';

  const APP_CTX = w.APP_CTX || '';
  w.APP = w.APP || {};
  w.APP.useBackend = true;
  w.APP.channelMap = w.APP.channelMap || { '프로젝트 1': 1, '프로젝트 2': 2, '프로젝트 3': 3 };

  const currentUser = { id: (w.CURRENT_USER_ID || 1), name: '나' };
  const $ = (sel, el = d) => el.querySelector(sel);

  // ---- 입력 엘리먼트 견고한 탐색 (모달 내 우선 → 전체) ----
  function getChatInputEl() {
    const modal = d.getElementById('chatModal') || d;
    // 우선순위 높은 셀렉터들
    return (
      modal.querySelector('#chatTextInput') ||
      modal.querySelector('.chat-input input[type="text"]') ||
      d.querySelector('#chatTextInput') ||
      d.querySelector('.chat-input input[type="text"]')
    );
  }

  // ---- 상태 ----
  let client = null;
  let subscription = null;
  let currentChannelId = null;

  w.__chatDbg = w.__chatDbg || {};
  w.__chatDbg.getState = () => ({ connected: !!(client && client.connected), currentChannelId });

  // ---- 렌더 ----
  const escapeHTML = (s='') =>
    s.replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));

  function appendMsg({ text, authorId, createdAt }) {
    const chatBox = $('#chatMessages');
    if (!chatBox) return;
    const side = (authorId === currentUser.id) ? 'right' : 'left';
    const div = d.createElement('div');
    div.className = `message ${side}`;
    const time = createdAt ? new Date(createdAt).toLocaleTimeString('ko-KR', {
      hour: 'numeric', minute: 'numeric', hour12: true
    }) : '';
    div.innerHTML = `
      ${side === 'left' ? `<img src="/images/profile1.png" class="avatar">` : ''}
      <div class="bubble">
        <div class="meta">
          <span class="sender">${side === 'right' ? '나' : '상대'}</span>
          <span class="time">${time}</span>
        </div>
        <p>${escapeHTML(text)}</p>
      </div>
      ${side === 'right' ? `<img src="/images/my-cat.png" class="avatar">` : ''}
    `;
    chatBox.appendChild(div);
    chatBox.scrollTop = chatBox.scrollHeight;
  }

  // ---- STOMP ----
  function ensureWs() {
    if (client && client.connected) return Promise.resolve();
    return new Promise((resolve) => {
      const sock = new SockJS((w.APP_CTX || '') + '/ws-chat');
      client = new StompJs.Client({
        webSocketFactory: () => sock,
        reconnectDelay: 3000,
        debug: (m) => console.log('[stomp]', m)
      });
      client.onConnect = () => { console.log('[chat] STOMP connected'); resolve(); };
      client.onStompError = (f) => console.error('[chat] STOMP error', f);
      client.onWebSocketError = (e) => console.error('[chat] WS error', e);
      client.activate();
      w.__chatDbg.client = client;
    });
  }

  async function enterChannel(channelId) {
    await ensureWs();
    if (subscription) { subscription.unsubscribe(); subscription = null; }
    currentChannelId = Number(channelId);
    console.log('[chat] enterChannel ->', currentChannelId);

    const chatBox = $('#chatMessages');
    if (chatBox) chatBox.innerHTML = '';
    fetch(`${APP_CTX}/api/messages/channels/${currentChannelId}?page=0&size=100`)
      .then(r => r.json())
      .then(list => list.forEach(m => appendMsg({
        text: m.body, authorId: m.authorId, createdAt: m.createdAt
      })))
      .catch(console.error);

    subscription = client.subscribe(`/topic/chat/${currentChannelId}`, (frame) => {
      const data = JSON.parse(frame.body);
      appendMsg({ text: data.body, authorId: data.authorId, createdAt: data.createdAt });
    });
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
      // 조합/커밋 지연 안전: animation frame → 120ms 후 재시도
      requestAnimationFrame(() => {
        setTimeout(() => {
          const retryVal = (getChatInputEl()?.value ?? '').trim();
          if (!retryVal) {
            console.warn('[chat] empty text even after retry');
            return;
          }
          _sendNow(retryVal);
        }, 120);
      });
      return;
    }
    _sendNow(firstTry);
  }

  function _sendNow(text) {
    if (!client || !client.connected) { console.warn('[chat] not connected'); return; }
    if (!currentChannelId) { console.warn('[chat] no channel selected'); return; }
    console.log('[chat] publish ->', { currentChannelId, text });
    client.publish({
      destination: `/app/chat/${currentChannelId}/send`,
      body: JSON.stringify({ channelId: currentChannelId, authorId: currentUser.id, body: text })
    });
    const el = getChatInputEl();
    if (el) el.value = '';
  }

  // ---- 방 선택 이벤트 ----
  w.addEventListener('chat:room-selected', (ev) => {
    const { roomName, channelId } = ev.detail || {};
    const cid = channelId || (w.APP.channelMap?.[roomName]);
    console.log('[chat] room-selected', ev.detail);
    if (!cid) { console.warn('채널 ID 없음:', roomName, channelId); return; }
    enterChannel(Number(cid));
  });

  // ---- 버튼/엔터(IME 안전) ----
  d.addEventListener('click', (e) => {
    const t = e.target;
    if (t && (t.id === 'btnChatSend' || t.closest('#btnChatSend'))) {
      e.preventDefault();
      // 클릭 즉시 읽고, 비면 재시도 루틴이 send() 안에서 처리됨
      send();
    }
  });

  // Enter 전송 (조합 중이면 무시)
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

})(window, document);
