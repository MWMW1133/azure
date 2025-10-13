(function (w) {
    'use strict';
    if (w.__meetingLoaded) return;
    w.__meetingLoaded = true;

    /* =========================================================
     * 공통 유틸 (CSRF + JSON fetch)
     * ======================================================= */
    function csrfHeaders(base = {}) {
        const t = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const h = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';
        return t ? { ...base, [h]: t } : base;
    }
    async function fetchJSON(url, opts = {}) {
        const resp = await fetch(url, {
            credentials: 'same-origin',
            headers: csrfHeaders({
                'Content-Type': 'application/json',
                ...(opts.headers || {})
            }),
            ...opts
        });
        if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
        return await resp.json().catch(() => ({}));
    }

    /* =========================================================
     * 스타일 / 마크업 (기존 그대로)
     * ======================================================= */
    function ensureRoomStyles() {
        if (document.getElementById('meeting-hud-styles')) return;
        const st = document.createElement('style');
        st.id = 'meeting-hud-styles';
        st.textContent = `
:root{ --border:#e6ebf3; --muted:#5b6b83; --ink:#0f172a; --ink2:#1f2937; --ink3:#334155; }
/* (생략) — 기존 CSS 전부 그대로 유지 */
`;
        document.head.appendChild(st);
    }

    function roomHTML() {
        return `
<section class="room-wrap">
  <!-- 로비(수락/거절) -->
  <section id="lobby-view" class="lobby" style="display:none">
    <div class="lobby-card">
      <div class="lobby-title">회의 참가하기</div>
      <p class="lobby-desc">이 회의에 참여하시겠습니까?</p>
      <div class="lobby-actions">
        <button id="btn-join" class="btn" type="button">수락</button>
        <button id="btn-decline" class="btn-dark" type="button">거절</button>
      </div>
    </div>
  </section>

  <div class="room-title">
    <select id="projectSelect" class="project-select" aria-label="프로젝트 선택">
      <option value="" selected disabled>프로젝트 선택</option>
    </select>
  </div>

  <div class="room-body">
    <div class="room-stage">
      <div class="stage-icon" aria-hidden="true">
        <span class="ring"></span><span class="ring r2"></span>
        <svg class="voice-mic" viewBox="0 0 48 48" role="img" aria-label="음성 회의 마이크">
          <defs>
            <linearGradient id="micGrad" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#7C3AED"/><stop offset="1" stop-color="#06B6D4"/></linearGradient>
            <filter id="micGlow" x="-60%" y="-60%" width="220%" height="220%"><feGaussianBlur stdDeviation="2.2" result="b"/><feMerge><feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge></filter>
          </defs>
          <g stroke="url(#micGrad)" stroke-width="3" fill="none" stroke-linecap="round" stroke-linejoin="round" filter="url(#micGlow)">
            <rect x="17" y="8" width="14" height="22" rx="7"/><path d="M12 22v1a12 12 0 0 0 24 0v-1"/><path d="M24 36v6M18 42h12"/>
          </g>
        </svg>
      </div>

      <!-- HUD -->
      <div class="stage-hud" role="toolbar" aria-label="회의 컨트롤">
        <div class="hud-bar">
          <div class="hud-left">
            <select class="hud-devsel" id="hud-mic"><option>기본 마이크</option></select>
          </div>
          <div class="hud-center">
            <button id="hud-notes-start" class="hud-btn primary">회의록 작성 시작</button>
            <button id="hud-notes-stop"  class="hud-btn" disabled>회의록 작성 종료</button>
            <button id="hud-open-notes"  class="hud-btn notes" type="button">회의록</button>
          </div>
          <div class="hud-right">
            <button id="hud-end" class="hud-btn danger">📞 통화 종료</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 우측 참가자 -->
    <aside class="room-people">
      <div class="rp-head">
        <div class="rp-title"><span class="rp-ic">👥</span><span>참가자</span><span class="rp-count" id="rp-count">0</span></div>
        <input class="rp-search" type="text" placeholder="검색" />
      </div>
      <ul class="rp-list"></ul>
      <button class="btn-ghost" id="btn-more">더보기</button>
    </aside>

    <!-- 회의록 모달 -->
    <div id="notes-modal" class="notes-modal" aria-hidden="true">
      <div class="notes-card" role="dialog" aria-modal="true" aria-labelledby="notes-title">
        <div class="notes-head">
          <strong id="notes-title" class="title">회의록</strong>
          <button id="btn-close-notes" type="button">닫기</button>
        </div>
        <div id="notes-body" class="notes-body">(아직 내용이 없습니다)</div>
        <div class="notes-actions">
          <button id="btn-clear-notes"  type="button">비우기</button>
          <button id="btn-export-notes" type="button">텍스트 저장</button>
        </div>
      </div>
    </div>
  </div>
</section>`;
    }

    /* =========================================================
     * 녹음/업로드/전사 컨트롤러 (UI는 그대로)
     * ======================================================= */
    const Recorder = (function(){
        let stream = null;
        let recorder = null;
        let chunks = [];
        let meeting = { id: null, upload: null }; // {id, upload:{url, method, headers, objectKey, fileUrl}}

        const preferredMime = (function(){
            const cand = ['audio/webm;codecs=opus', 'audio/webm', 'audio/mp4', 'audio/ogg'];
            const ok = cand.find(m => MediaRecorder.isTypeSupported?.(m));
            return ok || '';
        })();

        async function start(projectId) {
            // 1) 서버에 미팅 시작 (S3 프리사인 URL 받기)
            const startResp = await fetchJSON('/api/meetings/start', {
                method: 'POST',
                body: JSON.stringify({ projectId })
            });
            meeting.id = startResp.meetingId;
            meeting.upload = startResp.upload || {
                url: startResp.uploadUrl,
                method: startResp.uploadMethod || 'PUT',
                headers: startResp.uploadHeaders || {},
                objectKey: startResp.objectKey,
                fileUrl: startResp.fileUrl
            };

            // 2) 브라우저 마이크 캡처
            stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            chunks = [];
            recorder = new MediaRecorder(stream, preferredMime ? { mimeType: preferredMime } : undefined);
            recorder.ondataavailable = e => { if (e.data && e.data.size > 0) chunks.push(e.data); };
            recorder.start(250); // 타임슬라이스(조각)
        }

        async function stopAndUpload() {
            if (!recorder) return null;

            // stop()은 비동기: stop 이벤트를 기다렸다가 Blob 생성
            const stopped = new Promise(res => recorder.addEventListener('stop', res, { once: true }));
            recorder.stop();
            await stopped;

            // 스트림 정리
            try { stream?.getTracks().forEach(t => t.stop()); } catch(e){}

            const type = preferredMime || 'audio/webm';
            const blob = new Blob(chunks, { type });
            chunks = [];

            // 3) S3 업로드(프리사인 URL 사용, 기본은 PUT)
            const up = meeting.upload || {};
            if (!up.url) throw new Error('업로드 URL이 없습니다.');

            const putHeaders = { ...(up.headers || {}), 'Content-Type': blob.type || 'application/octet-stream' };
            const putResp = await fetch(up.url, { method: up.method || 'PUT', headers: putHeaders, body: blob });
            if (!putResp.ok) throw new Error(`S3 업로드 실패(${putResp.status})`);

            // fileUrl이 이미 오면 그걸 사용. 없으면, objectKey만 서버로 넘겨서 서버가 URL을 해석하도록 함
            const payload = {
                meetingId: meeting.id,
                objectKey: up.objectKey || null,
                fileUrl: up.fileUrl || null,
                contentType: blob.type
            };

            // 4) 서버에 전사 요청(Clova는 서버에서)
            const submit = await fetchJSON('/api/transcripts/submit', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            const transcriptId = submit.transcriptId || null;

            return { meetingId: meeting.id, transcriptId };
        }

        function currentMeetingId() { return meeting.id; }

        return { start, stopAndUpload, currentMeetingId };
    })();

    /* =========================================================
     * 참가자 패널(기존 스텁 유지, 나중에 API 연결)
     * ======================================================= */
    const currentUserId = 1;         // 예시
    const COMPANY_ADMIN_ID = 2;      // 예시

    const USERS = [
        { id: 1, name: '이재환' },
        { id: 2, name: '박소현' },
        { id: 3, name: '김형욱' },
        { id: 4, name: '양수빈' },
        { id: 5, name: '이민우' },
    ];
    const PROJECTS = [
        { id: 1, name: '프로젝트 1', members: [3,5] },
        { id: 2, name: '프로젝트 2', members: [1,3] },
        { id: 3, name: '프로젝트 3', members: [] },
    ];
    const INVITATIONS = { 1: [1], 2: [], 3: [] };

    const state = { users: USERS, projects: PROJECTS, invitations: INVITATIONS };

    async function apiListProjects(){ return state.projects; }
    async function apiListUsers(){ return state.users; }
    async function apiListProjectMemberIds(projectId){
        const p = state.projects.find(x => x.id === Number(projectId));
        const base = (p && Array.isArray(p.members)) ? p.members : [];
        return Array.from(new Set([COMPANY_ADMIN_ID, ...base]));
    }
    async function apiListProjectMembers(projectId){
        const ids   = await apiListProjectMemberIds(projectId);
        const users = await apiListUsers();
        const uniq  = Array.from(new Set(ids));
        return users.filter(u => uniq.includes(u.id));
    }
    async function apiListInvitations(projectId){ return state.invitations[projectId] || []; }
    async function apiInvite(projectId, userId){
        state.invitations[projectId] = Array.from(new Set([...(state.invitations[projectId]||[]), Number(userId)]));
        return { ok:true };
    }
    async function apiAccept(projectId, userId){
        const p = state.projects.find(x => x.id === Number(projectId));
        if (p){ p.members = Array.from(new Set([...(p.members||[]), Number(userId)])); }
        state.invitations[projectId] = (state.invitations[projectId]||[]).filter(id => id!==Number(userId));
        return { ok:true };
    }
    async function apiDecline(projectId, userId){
        state.invitations[projectId] = (state.invitations[projectId]||[]).filter(id => id!==Number(userId));
        return { ok:true };
    }
    async function apiRemove(projectId, userId){
        const p = state.projects.find(x => x.id === Number(projectId));
        if (p){ p.members = (p.members||[]).filter(id => id !== Number(userId)); }
        return { ok:true };
    }

    function getInitialProjectId() {
        const q = new URLSearchParams(location.search);
        return q.has('project') ? Number(q.get('project')) : null;
    }
    function setProjectUI(pid, root = document) {
        const sel = root.querySelector('#projectSelect');
        const exists = state.projects.find(p => p.id === Number(pid));
        if (sel && exists && sel.value !== String(pid)) sel.value = String(pid);
    }
    window.renderPeoplePanel = async function renderPeoplePanel(root = document, projectId){
        const listEl = root.querySelector('.rp-list'); if (!listEl) return;
        const cntEl  = root.querySelector('.rp-count, #rp-count');
        const [ids, users] = await Promise.all([ apiListProjectMemberIds(projectId), apiListUsers() ]);
        const memberIds = Array.from(new Set(ids));
        const members   = users.filter(u => memberIds.includes(u.id));
        const panel = root.querySelector('.room-people');
        if (panel) panel.classList.toggle('is-manager', currentUserId === COMPANY_ADMIN_ID);

        listEl.innerHTML = members.map(u => `
      <li class="person" data-id="${u.id}">
        <div class="avatar">${u.name[0]}</div>
        <div class="p-main">
          <div class="p-name">
            ${u.name} ${u.id === COMPANY_ADMIN_ID ? '<span class="role-badge">관리자</span>' : ''}
          </div>
          <div class="p-sub">온라인</div>
        </div>
        ${(currentUserId === COMPANY_ADMIN_ID && u.id !== COMPANY_ADMIN_ID)
            ? `<button class="chip" data-action="remove" data-id="${u.id}">제외</button>` : ''}
      </li>
    `).join('');
        if (cntEl) cntEl.textContent = String(members.length);

        // 초대 섹션(관리자만)
        if (currentUserId === COMPANY_ADMIN_ID){
            const candidates = users.filter(u => u.id !== COMPANY_ADMIN_ID && !memberIds.includes(u.id));
            if (candidates.length){
                listEl.insertAdjacentHTML('beforeend', `
          <li class="list-sep">초대 가능</li>
          ${candidates.map(u => `
            <li class="person candidate" data-id="${u.id}">
              <div class="avatar muted">${u.name[0]}</div>
              <div class="p-main"><div class="p-name">${u.name}</div><div class="p-sub muted">오프라인</div></div>
              <button class="chip" data-action="invite" data-id="${u.id}">초대</button>
            </li>
          `).join('')}
        `);
            }
            listEl.onclick = async (e) => {
                const btn = e.target.closest('.chip'); if (!btn) return;
                const uid = Number(btn.dataset.id);
                const action = btn.dataset.action;
                if (action === 'invite')      await apiInvite(getCurrentProjectId(), uid);
                else if (action === 'remove') await apiRemove(getCurrentProjectId(), uid);
                renderPeoplePanel(root, getCurrentProjectId());
            };
        }
    };

    function clearPeoplePanel(root=document){
        const list = root.querySelector('.rp-list, .room-people-list');
        const cnt  = root.querySelector('.rp-count, #rp-count');
        if (list) list.innerHTML = '';
        if (cnt)  cnt.textContent = '0';
    }
    function hideLobby(root=document){
        const lobby = root.querySelector('#lobby-view');
        if (lobby) lobby.style.display = 'none';
    }

    // 프로젝트 스위처
    async function initProjectSwitcher(root = document){
        const sel = root.querySelector('#projectSelect');
        // 서버에서 프로젝트 목록(지금은 스텁)
        const projects = await apiListProjects();
        sel.innerHTML = `<option value="" disabled ${!getInitialProjectId() ? 'selected' : ''}>프로젝트 선택</option>`
            + projects.map(p => `<option value="${p.id}">${p.name}</option>`).join('');

        const initialId = getInitialProjectId();
        if (initialId){
            setProjectUI(initialId, root);
            renderPeoplePanel(root, initialId);
            gateAccessForCurrentUser(root, initialId);
        }else{
            setProjectUI(null, root);
            clearPeoplePanel(root);
            hideLobby(root);
        }

        sel.addEventListener('change', (e) => {
            const pid = Number(e.target.value);
            setProjectUI(pid, root);
            renderPeoplePanel(root, pid);
            gateAccessForCurrentUser(root, pid);

            const url = new URL(location.href);
            url.searchParams.set('project', String(pid));
            history.pushState({}, '', url);
        });
    }

    // 로비/게이트
    function openLobby(root, { title, desc, acceptLabel, declineLabel, onAccept, onDecline, acceptDisabled }){
        const lobby = root.querySelector('#lobby-view'); if (!lobby) return;
        const t  = lobby.querySelector('.lobby-title');
        const d  = lobby.querySelector('.lobby-desc');
        let ok   = lobby.querySelector('#btn-join');
        let no   = lobby.querySelector('#btn-decline');

        t.textContent = title || '확인';
        d.textContent = desc || '';
        const okNew = ok.cloneNode(true), noNew = no.cloneNode(true);
        ok.replaceWith(okNew); no.replaceWith(noNew);
        ok = okNew; no = noNew;
        ok.textContent = acceptLabel || '확인';
        no.textContent = declineLabel || '취소';
        ok.disabled = !!acceptDisabled;

        ok.addEventListener('click', async()=>{ lobby.style.display='none'; await onAccept?.(); });
        no.addEventListener('click', async()=>{ lobby.style.display='none'; await onDecline?.(); });

        lobby.style.display = 'flex';
    }

    async function gateAccessForCurrentUser(root, projectId){
        const lobby = root.querySelector('#lobby-view'); if (!lobby) return;
        const memberIds  = await apiListProjectMemberIds(projectId);
        const invitedIds = await apiListInvitations(projectId);
        const isAdmin    = (currentUserId === COMPANY_ADMIN_ID);
        const invited    = invitedIds.includes(currentUserId);

        if (isAdmin || memberIds.includes(currentUserId)) { lobby.style.display='none'; return; }

        if (invited) {
            openLobby(root, {
                title:'프로젝트 참여 수락',
                desc:'관리자가 보낸 초대를 수락하면 이 프로젝트 회의실을 사용할 수 있어요.',
                acceptLabel:'수락', declineLabel:'닫기',
                onAccept: async()=>{ await apiAccept(projectId, currentUserId); renderPeoplePanel(root, projectId); },
                onDecline: async()=>{ await apiDecline(projectId, currentUserId); showCancelCard('참가 요청이 취소되었습니다.'); }
            });
        } else {
            openLobby(root, {
                title:'초대 필요', desc:'회사 관리자에게 초대를 요청하세요.',
                acceptLabel:'확인', declineLabel:'닫기', acceptDisabled:true
            });
        }
    }

    // 종료 토스트
    function showEndToastAtHangup(message, anchorBtn){
        let toast = document.getElementById('end-toast');
        if (!toast) { toast = document.createElement('div'); toast.id='end-toast'; toast.className='end-toast'; document.body.appendChild(toast); }
        toast.textContent = message;
        toast.classList.add('anchored'); toast.style.right=''; toast.style.bottom='';
        const btn = anchorBtn || document.querySelector('#hud-end');
        if (btn) {
            const r = btn.getBoundingClientRect();
            toast.style.left = (r.right + 28) + 'px';
            toast.style.top  = r.top + 'px';
            requestAnimationFrame(() => {
                const h = toast.offsetHeight || 40;
                toast.style.top = Math.round(r.top + (r.height - h) / 2) + 'px';
                toast.classList.add('in');
            });
        } else {
            toast.classList.remove('anchored'); toast.style.right='24px'; toast.style.bottom='24px';
            requestAnimationFrame(()=> toast.classList.add('in'));
        }
        clearTimeout(showEndToastAtHangup._timer);
        showEndToastAtHangup._timer = setTimeout(()=> toast.classList.remove('in'), 1800);
    }

    // 취소 카드 (기존)
    (function setupCancelCard(){
        function ensureCancelStyles() {
            if (document.getElementById('cancel-card-style')) return;
            const s = document.createElement('style');
            s.id = 'cancel-card-style';
            s.textContent = `.cancel-state{display:grid;place-items:center;min-height:calc(100dvh - 150px);} .cancel-card{display:flex;gap:12px;padding:18px 22px;background:#fff;border:1px solid #e6ebf3;border-radius:16px;box-shadow:0 12px 28px rgba(28,39,71,.10);color:#334155;font-weight:600;animation:cc-in .22s ease-out both;} @keyframes cc-in{from{transform:translateY(6px);opacity:0}to{transform:translateY(0);opacity:1}}`;
            document.head.appendChild(s);
        }
        w.__ensureCancelStyles__ = ensureCancelStyles;
    })();
    w.showCancelCard = function showCancelCard(message='입장이 취소되었습니다.'){
        (w.__ensureCancelStyles__||function(){})();
        document.querySelectorAll('.cancel-layer').forEach(n=>n.remove());
        const layer = document.createElement('div'); layer.className='cancel-layer';
        const state = document.createElement('div'); state.className='cancel-state';
        const card  = document.createElement('div'); card.className='cancel-card';
        const ico = document.createElement('div');
        Object.assign(ico.style,{display:'grid',placeItems:'center',width:'40px',height:'40px',borderRadius:'999px',background:'linear-gradient(180deg,#fca5a5,#ef4444)',color:'#fff',fontSize:'18px',boxShadow:'inset 0 0 0 1px rgba(255,255,255,.35)'}); ico.textContent='⛔';
        const txt = document.createElement('div'); txt.textContent=message;
        card.appendChild(ico); card.appendChild(txt); state.appendChild(card); layer.appendChild(state);
        document.body.appendChild(layer);
        setTimeout(()=>{ layer.classList.add('hide'); setTimeout(()=>layer.remove(),220); },1600);
        return layer;
    };
    document.addEventListener('click', (e) => {
        const declineBtn = e.target.closest('[data-action="decline"], .btn-decline, .btn-cancel, #btn-invite-decline');
        if (!declineBtn) return; w.showCancelCard?.('참가 요청이 취소되었습니다.');
    });

    /* =========================================================
     * 이벤트 바인딩(핵심: 버튼 동작만 교체)
     * ======================================================= */
    function bindRoomEvents(root){
        const startBtn = root.querySelector('#hud-notes-start');
        const stopBtn  = root.querySelector('#hud-notes-stop');
        const openBtn  = root.querySelector('#hud-open-notes');
        const endBtn   = root.querySelector('#hud-end');
        const micSel   = root.querySelector('#hud-mic');

        const notesModal  = document.getElementById('notes-modal');
        const notesBody   = document.getElementById('notes-body');
        const closeNotes  = document.getElementById('btn-close-notes');
        const clearNotes  = document.getElementById('btn-clear-notes');
        const exportNotes = document.getElementById('btn-export-notes');

        function openNotes(){ notesModal.setAttribute('aria-hidden','false'); }
        function closeNotesFn(){ notesModal.setAttribute('aria-hidden','true'); }

        openBtn?.addEventListener('click', openNotes);
        closeNotes?.addEventListener('click', closeNotesFn);
        clearNotes?.addEventListener('click', ()=> notesBody.textContent = '(아직 내용이 없습니다)');
        exportNotes?.addEventListener('click', ()=>{
            const blob = new Blob([notesBody.textContent||''], {type:'text/plain;charset=utf-8'});
            const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = 'meeting-notes.txt'; a.click(); URL.revokeObjectURL(a.href);
        });

        // 진행상태 표시 헬퍼
        function setStatus(text, append=false){
            if (!notesBody) return;
            if (append) notesBody.textContent = (notesBody.textContent==='(아직 내용이 없습니다)' ? '' : notesBody.textContent) + '\n' + text;
            else notesBody.textContent = text;
        }

        // 회의록 시작
        startBtn?.addEventListener('click', async (e) => {
            e.preventDefault();
            const pid = getCurrentProjectId?.();
            if (!pid) { alert('프로젝트를 먼저 선택하세요.'); return; }

            // 멤버십 게이트(스텁)
            const ids = await apiListProjectMemberIds(pid);
            const isAdmin = currentUserId === COMPANY_ADMIN_ID;
            if (!isAdmin && !ids.includes(currentUserId)) {
                gateAccessForCurrentUser(document, pid);
                return;
            }

            try{
                startBtn.disabled = true; stopBtn.disabled = true;
                setStatus('🎙️ 마이크 준비 중...', false); openNotes();
                await Recorder.start(pid);
                setStatus('• 녹음 중...', false);
                stopBtn.disabled = false;
                root.classList.add('rec-on');               // 네온 링 ON
                (document.querySelector('.room-wrap')||document).classList.add('in-call');
            }catch(err){
                console.error(err);
                startBtn.disabled = false; stopBtn.disabled = true;
                setStatus('마이크 접근/녹음 시작에 실패했습니다.');
                alert('녹음을 시작할 수 없습니다.\n브라우저 권한과 HTTPS 환경을 확인하세요.');
            }
        });

        // 회의록 종료 → 업로드 → 전사 요청 → 폴링
        stopBtn?.addEventListener('click', async () => {
            try{
                startBtn.disabled = true; stopBtn.disabled = true;
                root.classList.remove('rec-on');             // 네온 링 OFF
                setStatus('⏹️ 녹음 종료. 업로드 중...', false);

                const { meetingId, transcriptId } = await Recorder.stopAndUpload();
                setStatus('☁️ 업로드 완료. 전사 요청 중...');

                // 폴링: /api/meetings/{id}/transcript 가 {status,text}를 준다고 가정
                await pollTranscript(meetingId, (state) => {
                    if (state === 'PENDING') setStatus('🧠 전사/요약 중... (잠시만요)', false);
                }).then(text => {
                    setStatus(text || '(전사 결과 없음)');
                    requestAnimationFrame(()=> document.getElementById('hud-open-notes')?.classList.add('show'));
                });

            }catch(err){
                console.error(err);
                setStatus('업로드/전사 요청 중 오류가 발생했습니다.');
                startBtn.disabled = false; stopBtn.disabled = true;
            }finally{
                startBtn.disabled = false; stopBtn.disabled = true;
            }
        });

        // 통화 종료: 서버에 종료 알림(선택), 토스트
        endBtn?.addEventListener('click', async () => {
            try{
                const mid = Recorder.currentMeetingId?.();
                if (mid) {
                    fetchJSON(`/api/meetings/${mid}/finish`, { method:'POST', body: JSON.stringify({}) }).catch(()=>{});
                }
            }finally{
                showEndToastAtHangup('회의가 종료되었습니다.', endBtn);
            }
        });

        // 마이크 디바이스 리스트
        try{
            if (navigator.mediaDevices?.enumerateDevices) {
                navigator.mediaDevices.enumerateDevices().then(list=>{
                    const mics = list.filter(d=>d.kind==='audioinput');
                    if (mics.length && micSel){
                        micSel.innerHTML = mics.map(d=>`<option value="${d.deviceId}">${d.label || '마이크'}</option>`).join('');
                    }
                });
            }
        }catch(e){}
    }

    // 전사 폴링
    async function pollTranscript(meetingId, onPending){
        const wait = ms => new Promise(r=>setTimeout(r,ms));
        const maxWaitMs = 1000 * 60 * 10; // 최대 10분
        const started = Date.now();

        while (Date.now() - started < maxWaitMs) {
            const resp = await fetchJSON(`/api/meetings/${meetingId}/transcript`, { method:'GET' });
            const status = (resp.status || 'PENDING').toUpperCase();
            if (status === 'DONE') return resp.text || '';
            if (status === 'ERROR') throw new Error('전사 실패');
            onPending?.('PENDING');
            await wait(4000);
        }
        throw new Error('전사 시간 초과');
    }

    /* =========================================================
     * Mount
     * ======================================================= */
    const Meeting = {
        mount(target){
            if (w.__meetingMounted) return;
            w.__meetingMounted = true;

            const el = (typeof target === 'string') ? document.querySelector(target) : target;
            if (!el) return;
            ensureRoomStyles();
            el.innerHTML = roomHTML();

            const wrap = el.querySelector('.room-wrap');
            bindRoomEvents(wrap);
            initProjectSwitcher(wrap);
        }
    };
    w.Meeting = Meeting;

})(window);

// === JSP에서 읽는 함수: 현재 선택 프로젝트 ===
function getCurrentProjectId() {
    const sel = document.querySelector('#projectSelect');
    return sel ? Number(sel.value) : null;
}
