// =============================================================
// 헬퍼 함수 (반드시 메인 로직보다 위에 있어야 합니다)
// =============================================================

function CFG() {
    const d = (document.getElementById('meeting-config') || { dataset: {} }).dataset;
    return {
        meUrl: d.meUrl,
        projectsUrl: d.projectsUrl,
        projectMembersUrl: (id) => tpl(d.projectMembersUrl, { id }),
        projectInvitationsUrl: (id) => tpl(d.projectInvitationsUrl, { id }),
        projectInviteUrl: (id) => tpl(d.projectInviteUrl, { id }),
        projectAcceptUrl: (id) => tpl(d.projectAcceptUrl, { id }),
        projectDeclineUrl: (id, userId) => tpl(d.projectDeclineUrl, { id, userId }),
        projectRemoveUrl: (id, userId) => tpl(d.projectRemoveUrl, { id, userId }),
        presignUrl: d.presignUrl,
        publicBaseUrl: d.publicBaseUrl,
        startUrl: d.startUrl,
        endUrl: d.endUrl,
        submitUrl: d.submitUrl,
        // --- 👇 Agora 관련 설정 추가 ---
        agoraAppId: d.agoraAppId,
        agoraTokenUrl: (channel) => tpl(d.agoraTokenUrl, { channel })
    };
}

function tpl(t,obj){
    if (typeof t !== 'string') return '';
    return t.replace(/\{(\w+)\}/g,(_,k)=>encodeURIComponent(obj[k]??''));
}

async function api(method, url, body, headers={}){
    if (!url) {
        throw new Error(`API call aborted: URL is ${url}`);
    }
    const res = await fetch(url, {
        method, credentials:'same-origin',
        headers:{ 'Content-Type':'application/json', ...headers },
        body: body ? JSON.stringify(body) : undefined
    });
    if(!res.ok) throw new Error(`${method} ${url} -> ${res.status}`);
    const ct = res.headers.get('content-type')||'';
    return ct.includes('application/json') ? res.json() : res.text();
}

async function apiMe(){
    if (window.__ME) return window.__ME;
    window.__ME = await api('GET', CFG().meUrl);
    return window.__ME;
}

async function apiListProjects(){
    const list = await api('GET', CFG().projectsUrl + '/list');
    return Array.isArray(list) ? list : [];
}

function isAdmin(user) {
    return user && (user.role === 'ADMIN' || user.role === 'MANAGER');
}

async function apiListProjectMembers(projectId){
    const url = CFG().projectMembersUrl(projectId);
    const res = await api('GET', url);
    if (!Array.isArray(res)) return [];
    if (typeof res[0] === 'number') {
        return res.map(id => ({ id, name: `유저#${id}` }));
    }
    return res;
}

async function apiListProjectMemberIds(projectId){
    const members = await apiListProjectMembers(projectId);
    return members.map(m => Number(m.id)).filter(Number.isFinite);
}

async function apiListInvitations(projectId){
    const url = CFG().projectInvitationsUrl(projectId);
    const res = await api('GET', url);
    return Array.isArray(res) ? res.map(Number) : [];
}

async function apiAccept(projectId, userId){
    const url = CFG().projectAcceptUrl(projectId);
    return api('POST', url, { userId });
}

async function apiDecline(projectId, userId){
    const url = CFG().projectDeclineUrl(projectId, userId);
    return api('DELETE', url);
}

async function apiRemove(projectId, userId){
    const url = CFG().projectRemoveUrl(projectId, userId);
    return api('DELETE', url);
}

function getInitialProjectId(){
    const q = new URLSearchParams(location.search);
    const v = q.get('project') || q.get('projectId');
    return v ? Number(v) : null;
}

function getSelectedProjectId(root=document){
    const sel = root.querySelector('#projectSelect');
    return sel && sel.value ? Number(sel.value) : null;
}

// =============================================================
// 메인 로직
// =============================================================

(function (w) {
    'use strict';
    if (w.__meetingLoaded) return;
    w.__meetingLoaded = true;

    function ensureRoomStyles() {
        if (document.getElementById('meeting-hud-styles')) return;
        const st = document.createElement('style');
        st.id = 'meeting-hud-styles';
        st.textContent = `
:root{--border:#e6ebf3;--muted:#5b6b83;--ink:#0f172a;--ink2:#1f2937;--ink3:#334155;}
.room-topbar{padding:14px 4px 10px;display:flex;justify-content:flex-start;align-items:center;min-height:56px;}
.room-title{font-size:clamp(22px,2.0vw,30px);line-height:1.25;font-weight:800;color:var(--ink2);letter-spacing:-.01em;text-align:left;margin-left:18px;}
.room-body{display:grid;grid-template-columns:1fr 340px;gap:24px;align-items:stretch;min-height:835px;margin-top:6px;}
.room-stage{position:relative;background:#fff;border:1px solid var(--border);border-radius:16px;box-shadow:0 8px 20px rgba(28,39,71,.06);display:flex;align-items:center;justify-content:center;min-height:700px;}
.stage-icon{position:relative;width:220px;height:220px;display:flex;align-items:center;justify-content:center;}
.voice-mic{width:96px;height:96px;position:relative;z-index:2;}
.ring{position:absolute;left:50%;top:50%;width:180px;height:180px;border-radius:999px;transform:translate(-50%,-50%);background:radial-gradient(closest-side,rgba(124,58,237,.65),rgba(6,182,212,.38),transparent 70%);animation:ring-pulse 2.4s infinite ease-out;animation-play-state:paused;opacity:0;z-index:1;pointer-events:none;}
.room-wrap.rec-on .ring{opacity:1;animation-play-state:running;}
.ring.r2{animation-delay:.9s;}
@keyframes ring-pulse{0%{transform:translate(-50%,-50%) scale(.82);opacity:0}10%{opacity:.9}100%{transform:translate(-50%,-50%) scale(1.25);opacity:0}}
.room-people{background:#fff;border:1px solid var(--border);border-radius:16px;box-shadow:0 8px 20px rgba(28,39,71,.06);padding:16px 14px;display:flex;flex-direction:column;}
.rp-head{display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;}
.rp-title{display:flex;align-items:center;gap:8px;font-weight:800;color:var(--ink);}
.rp-count{margin-left:4px;background:#eef3ff;border-radius:999px;padding:2px 8px;font-size:12px;}
.rp-search{height:32px;border:1px solid #dbe2ef;border-radius:10px;padding:0 10px;outline:none;font-size:13px;min-width:140px;}
.room-people-list{list-style:none;margin:0;padding:6px 2px 10px;display:flex;flex-direction:column;gap:12px;overflow:auto;flex:1 1 auto;}
.person{display:flex;align-items:center;gap:12px;padding:6px 8px;border-radius:10px;transition:background .15s,box-shadow .15s;}
.person:hover{background:#f8fafc;box-shadow:inset 0 0 0 1px #e5e7eb;}
.avatar{width:42px;height:42px;border-radius:999px;display:grid;place-items:center;color:#fff;font-weight:800;box-shadow:inset 0 0 0 1px rgba(255,255,255,.35);background:linear-gradient(180deg,#94a3ff,#6366f1);}
.av-1{background:linear-gradient(180deg,#60a5fa,#3b82f6);}
.av-2{background:linear-gradient(180deg,#34d399,#10b981);}
.av-3{background:linear-gradient(180deg,#f472b6,#ec4899);}
.av-4{background:linear-gradient(180deg,#fbbf24,#f59e0b);}
.p-main{flex:1 1 auto;min-width:0;}
.p-name{font-weight:700;color:var(--ink2);letter-spacing:-.01em;}
.p-sub{font-size:12px;color:#64748b;margin-top:2px;}
.p-actions{display:flex;gap:6px;margin-left:auto;justify-content:flex-end;min-width:76px;opacity:0;visibility:hidden;transform:translateX(6px);transition:opacity .15s,transform .15s;pointer-events:none;}
.person:hover .p-actions,.person:focus-within .p-actions{opacity:1;visibility:visible;transform:none;pointer-events:auto;}
.chip{border:1px solid rgba(0,0,0,.08);background:#f8fafc;border-radius:10px;height:28px;padding:0 8px;display:inline-flex;align-items:center;gap:4px;cursor:pointer;}
.chip:hover{filter:brightness(1.02);box-shadow:0 1px 4px rgba(0,0,0,.06);}
.chip:active{transform:translateY(1px);}
.controls{display:none!important;}
#lobby-view{display:none;position:absolute;inset:0;align-items:center;justify-content:center;background:rgba(15,23,42,.35);-webkit-backdrop-filter:blur(10px) saturate(140%);backdrop-filter:blur(10px) saturate(140%);z-index:100;padding:0 24px;}
.room-wrap{position:relative;}
#lobby-view .lobby-card{width:440px;max-width:90vw;padding:26px 24px 20px;border-radius:20px;border:1px solid rgba(15,23,42,.08);background:linear-gradient(180deg,rgba(255,255,255,.94),rgba(250,251,255,.88));box-shadow:0 24px 60px rgba(6,12,31,.25),0 1px 0 rgba(255,255,255,.75) inset;transition:transform .2s ease,box-shadow .2s ease;}
.lobby-title{font-size:20px;font-weight:800;letter-spacing:-.01em;color:#0f172a;margin:4px 0 10px;}
.lobby-desc{color:#5b6b83;margin-bottom:18px;}
.lobby-actions{display:flex;gap:10px;justify-content:center;padding-top:6px;}
#btn-join{background:linear-gradient(90deg,#4f8cff,#2f6bff);color:#fff;border:0;padding:12px 20px;border-radius:12px;box-shadow:0 10px 22px rgba(47,107,255,.28),inset 0 1px 0 rgba(255,255,255,.28);}
#btn-decline{background:linear-gradient(180deg,#f8fafc,#eef2f7);color:#0f172a;border:1px solid rgba(15,23,42,.12);padding:12px 20px;border-radius:12px;}
.end-toast{position:fixed;right:24px;bottom:24px;z-index:9999;background:rgba(17,24,39,.92);color:#fff;padding:12px 16px;border-radius:12px;border:1px solid rgba(255,255,255,.08);box-shadow:0 14px 34px rgba(2,8,23,.3);backdrop-filter:blur(6px) saturate(130%);opacity:0;transform:translateY(8px);transition:opacity .2s,transform .2s;}
.end-toast.in{opacity:1;transform:translateY(0);}
.end-toast.anchored{right:auto;bottom:auto;transform:none;}
.end-toast.anchored::after{content:"";position:absolute;left:-8px;top:50%;transform:translateY(-50%);border-width:8px;border-style:solid;border-color:transparent #111827 transparent transparent;}
.notes-modal{position:fixed;inset:0;display:none;align-items:center;justify-content:center;background:rgba(15,23,42,.4);backdrop-filter:blur(6px) saturate(130%);-webkit-backdrop-filter:blur(6px) saturate(130%);z-index:2000;box-sizing:border-box;padding:clamp(16px,3vw,28px);}
.notes-modal[aria-hidden="false"]{display:flex!important;}
@keyframes notes-pop{from{transform:translateY(8px) scale(.985);opacity:0}to{transform:none;opacity:1}}
.notes-card{width:min(920px,96vw);height:min(76vh,760px);position:relative;display:flex;flex-direction:column;overflow:hidden;border-radius:18px;background:linear-gradient(180deg,rgba(255,255,255,.92),rgba(248,250,255,.86));backdrop-filter:blur(8px) saturate(140%);-webkit-backdrop-filter:blur(8px) saturate(140%);box-shadow:0 26px 80px rgba(2,8,23,.28),0 2px 0 rgba(255,255,255,.6) inset;animation:notes-pop .18s ease both;}
.notes-card::before{content:"";position:absolute;inset:0;border-radius:inherit;pointer-events:none;padding:1px;background:linear-gradient(135deg,#c7d2fe 0%,#93c5fd 40%,#a7f3d0 100%);-webkit-mask:linear-gradient(#000 0 0) content-box,linear-gradient(#000 0 0);-webkit-mask-composite:xor;mask-composite:exclude;}
.notes-head{position:sticky;top:0;z-index:1;display:flex;align-items:center;justify-content:space-between;padding:14px 16px;background:linear-gradient(180deg,rgba(255,255,255,.96),rgba(250,252,255,.9));border-bottom:1px solid rgba(2,8,23,.06);backdrop-filter:blur(4px) saturate(120%);}
.notes-head .title{font-weight:800;letter-spacing:-.01em;color:#0f172a;}
.notes-head button,.notes-actions button{height:38px;padding:0 14px;border-radius:12px;border:1px solid rgba(2,8,23,.1);background:linear-gradient(180deg,#f8fafc,#eef2f7);color:#0f172a;font-weight:700;cursor:pointer;}
.notes-body{flex:1;overflow:auto;padding:16px 18px 20px;font-size:14.5px;line-height:1.68;color:#334155;white-space:pre-wrap;}
.notes-body::-webkit-scrollbar{width:12px;}
.notes-body::-webkit-scrollbar-thumb{background:linear-gradient(180deg,#c7d2fe,#a5b4fc);border-radius:999px;border:3px solid transparent;background-clip:content-box;}
.notes-actions{display:flex;gap:10px;justify-content:flex-end;padding:12px 16px;border-top:1px solid rgba(2,8,23,.06);background:linear-gradient(180deg,rgba(255,255,255,.92),rgba(248,250,255,.88));}
.notes-actions button:last-child{background:linear-gradient(90deg,#60a5fa,#3b82f6);color:#fff;border-color:#3b82f6;box-shadow:0 10px 22px rgba(59,130,246,.22),inset 0 0 0 1px rgba(255,255,255,.25);}
.stage-hud{position:absolute;left:50%;bottom:18px;transform:translate(-50%,12px);opacity:0;pointer-events:none;transition:opacity .16s,transform .16s;}
.room-stage:hover .stage-hud,.stage-hud:hover{opacity:1;transform:translate(-50%,0);pointer-events:auto;}
.hud-bar{display:flex;align-items:center;gap:12px;background:linear-gradient(180deg,#0b0f14 0%,#090d12 100%);color:#fff;border:1px solid rgba(255,255,255,.1);border-radius:14px;padding:10px 12px;box-shadow:0 12px 28px rgba(2,8,23,.4);backdrop-filter:blur(6px) saturate(125%);}
.hud-left,.hud-center,.hud-right{display:flex;align-items:center;gap:10px;}
.hud-left{padding-right:8px;border-right:1px solid rgba(255,255,255,.1);}
.hud-right{padding-left:8px;border-left:1px solid rgba(255,255,255,.1);}
.hud-btn{height:44px;padding:0 18px;border-radius:12px;border:1px solid rgba(255,255,255,.16);background:rgba(255,255,255,.08);color:#fff;cursor:pointer;font-size:15px;font-weight:700;white-space:nowrap;letter-spacing:-.01em;}
.hud-btn:hover{background:rgba(255,255,255,.16);}
.hud-btn.primary{background:linear-gradient(90deg,#60a5fa,#3b82f6);border-color:#3b82f6;}
.hud-btn.danger{background:#ef4444;border-color:#ef4444;}
.hud-btn:disabled{opacity:.55;cursor:not-allowed;}
.hud-devsel{height:44px;min-width:190px;padding:0 12px;border-radius:12px;border:1px solid rgba(255,255,255,.16);background:rgba(255,255,255,.08);color:#fff;white-space:nowrap;}
.hud-btn.notes{width:0;padding:0;margin-left:0;opacity:0;overflow:hidden;transition:width .18s ease,padding .18s ease,margin .18s ease,opacity .18s ease;}
.hud-btn.notes.show{width:auto;padding:0 18px;margin-left:8px;opacity:1;}
.cancel-state{display:grid;place-items:center;min-height:calc(100dvh - 150px);}
.cancel-card{display:flex;align-items:center;gap:12px;padding:18px 22px;background:#fff;border:1px solid #e6ebf3;border-radius:16px;box-shadow:0 12px 28px rgba(28,39,71,.1);color:#334155;font-weight:600;letter-spacing:.2px;animation:cc-in .22s ease-out both;}
.cancel-card::before{content:"⛔";display:grid;place-items:center;width:40px;height:40px;border-radius:999px;background:linear-gradient(180deg,#fca5a5,#ef4444);color:#fff;font-size:18px;box-shadow:inset 0 0 0 1px rgba(255,255,255,.35);}
.cancel-layer{position:fixed;inset:0;z-index:2147483647;display:grid;place-items:center;background:rgba(248,250,252,.92);backdrop-filter:saturate(120%) blur(1px);animation:cancel-fade-in .12s ease-out both;}
.cancel-layer.hide{animation:cancel-fade-out .18s ease-in both;}
@keyframes cancel-fade-in{from{opacity:0}to{opacity:1}}
@keyframes cancel-fade-out{to{opacity:0;transform:translateY(4px)}}
.project-select{font-weight:700;font-size:20px;line-height:1.2;color:var(--text,#111827);border:0;background:transparent;box-shadow:none;padding:2px 28px 2px 2px;border-radius:0;min-width:140px;appearance:none;-webkit-appearance:none;-moz-appearance:none;background-image:url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='18' height='18' viewBox='0 0 24 24' fill='none' stroke='%235b6b83' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><polyline points='6 9 12 15 18 9'/></svg>");background-repeat:no-repeat;background-position:right 6px center;background-size:16px 16px;}
.project-select:hover{border:0;box-shadow:none;background:transparent;}
.project-select:focus{outline:none;border:0;box-shadow:none;background:transparent;}
.project-select option{font-weight:600;font-size:16px;background:#fff;color:#111827;}
.project-select option:checked{background:#eef2ff;color:#1f2937;}
.room-title #projectTitle{display:none;}
.role-badge{margin-left:6px;padding:2px 6px;border-radius:999px;font-size:12px;font-weight:600;background:#eef2ff;color:#374151;}
.person .invite-btn{margin-left:auto;padding:6px 10px;border:1px solid var(--border,#e6ebf3);border-radius:8px;background:#fff;color:#1f2937;cursor:pointer;}
.person .invite-btn:hover{background:#f8fafc;}
.person .invite-btn.remove{border-color:#fecaca;color:#991b1b;}
.list-sep{margin:10px 0 6px;padding:0 4px;font-size:12px;color:#6b7280;}
.room-people .rp-search{display:none;}
.room-people.is-manager .rp-search{display:block;}
@keyframes cc-in{from{transform:translateY(6px);opacity:0}to{transform:translateY(0);opacity:1}}
`;
        document.head.appendChild(st);
    }

    function roomHTML() {
        return `
<section class="room-wrap">
  <section id="lobby-view" class="lobby">
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
    <select id="projectSelect" class="project-select" aria-label="프로젝트 선택"></select>
   </div>
  <div class="room-body">
    <div class="room-stage">
      <div class="stage-icon" aria-hidden="true">
        <span class="ring"></span><span class="ring r2"></span>
        <svg class="voice-mic" viewBox="0 0 48 48" role="img" aria-label="음성 회의 마이크"><defs><linearGradient id="micGrad" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#7C3AED"/><stop offset="1" stop-color="#06B6D4"/></linearGradient><filter id="micGlow" x="-60%" y="-60%" width="220%" height="220%"><feGaussianBlur stdDeviation="2.2" result="b"/><feMerge><feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge></filter></defs><g stroke="url(#micGrad)" stroke-width="3" fill="none" stroke-linecap="round" stroke-linejoin="round" filter="url(#micGlow)"><rect x="17" y="8" width="14" height="22" rx="7"/><path d="M12 22v1a12 12 0 0 0 24 0v-1"/><path d="M24 36v6M18 42h12"/></g></svg>
      </div>
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
      <aside class="room-people">
        <div class="rp-head">
        <div class="rp-title">
          <span class="rp-ic">👥</span>
          <span>참가자</span>
          <span class="rp-count" id="rp-count">0</span>
        </div>
      <input class="rp-search" type="text" placeholder="검색" />
    </div>
      <ul class="rp-list"></ul>
    <button class="btn-ghost" id="btn-more">더보기</button>
    </aside>
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
    </section>`;
    }

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

        let mediaStream = null;
        let mediaRecorder = null;
        let chunks = [];
        let meetingId = null;


        // --- 👇 Agora 관련 변수 추가 ---
        let agoraClient = null;
        let localAudioTrack = null;
        // --- 👆 Agora 관련 변수 추가 ---

        async function startCapture(){
            console.log("🎤 startCapture: 캡처 및 Agora 연결을 시작합니다...");
            try {
                // 1. 로컬 녹음용 스트림 생성
                const deviceId = micSel && micSel.value ? { deviceId: { exact: micSel.value } } : true;
                mediaStream = await navigator.mediaDevices.getUserMedia({ audio: deviceId });
                console.log("✅ 마이크 권한 획득 성공!");
                chunks = [];
                mediaRecorder = new MediaRecorder(mediaStream, { mimeType: 'audio/webm' });
                mediaRecorder.ondataavailable = (e)=>{ if (e.data && e.data.size>0) chunks.push(e.data); };
                mediaRecorder.start(1000);
                console.log("⏺️ 로컬 녹음 시작.");

                // --- 👇 Agora 로직 ---
                const config = CFG();
                if (!config.agoraAppId) {
                    console.warn("Agora App ID가 설정되지 않았습니다. 실시간 음성통화를 건너뜁니다.");
                    return; // App ID 없으면 실행 중단
                }

                agoraClient = AgoraRTC.createClient({ mode: "rtc", codec: "vp8" });

                agoraClient.on("user-published", async (user, mediaType) => {
                    await agoraClient.subscribe(user, mediaType);
                    if (mediaType === "audio") {
                        console.log("🔊 다른 참가자 오디오 수신:", user.uid);
                        user.audioTrack.play();
                    }
                });

                const channelName = `project-${getSelectedProjectId(root)}`;
                const userId = (await apiMe()).id;

                // ❗️ 토큰 서버가 있다면 여기서 토큰을 받아옵니다. 지금은 null로 진행합니다.
                // const { token } = await api('GET', config.agoraTokenUrl(channelName));
                const token = null;

                await agoraClient.join(config.agoraAppId, channelName, token, userId);
                console.log(`✅ Agora 채널 [${channelName}] 참가 성공.`);

                localAudioTrack = await AgoraRTC.createMicrophoneAudioTrack();
                await agoraClient.publish([localAudioTrack]);
                console.log("📢 내 마이크 오디오 발행 성공.");

            } catch (err) {
                console.error("❌ startCapture 실패!", err);
                alert("마이크/Agora 오류가 발생했습니다. 콘솔을 확인해주세요.");
            }
        }
        async function stopCapture(){
            if (!mediaRecorder) return null;
            await new Promise(res => { mediaRecorder.onstop = res; mediaRecorder.stop(); });
            mediaStream && mediaStream.getTracks().forEach(t=>t.stop());
            const blob = new Blob(chunks, { type: 'audio/webm' });
            chunks = []; mediaRecorder = null; mediaStream = null;
            return blob;
        }

        async function beginNotes(){
            const me = await apiMe();
            const orgId = me.organizationId || me.orgId;
            const pid = getSelectedProjectId(root);
            if (!pid){ alert('프로젝트를 먼저 선택하세요.'); return; }

            const created = await api('POST', CFG().startUrl, { organizationId: orgId, projectId: pid });
            meetingId = created.id || created.meetingId;

            await startCapture();

            startBtn.disabled = true;
            stopBtn.disabled  = false;
            root.classList.add('rec-on');
        }
        async function endNotes(){
            try{
                const me = await apiMe();
                const orgId = me.organizationId || me.orgId;
                const pid = getSelectedProjectId(root);

                const blob = await stopCapture();
                if (!blob) throw new Error('오디오 블랍이 없습니다.');

                const key = s3Key(orgId, pid, meetingId || 'na');
                const publicUrl = await uploadViaPresigned(blob, key);

                await api('POST', CFG().submitUrl, {
                    meetingId, audioUrl: publicUrl, mediaType: blob.type
                });
                if (meetingId) {
                    // ❗️ JSP에 정의된 END_URL은 /api/meetings 입니다.
                    // 백엔드 컨트롤러(@PostMapping("/{meetingId}/end"))에 맞게 URL을 완성합니다.
                    await api('POST', `${CFG().endUrl}/${encodeURIComponent(meetingId)}/end`);
                }

                openBtn?.classList.add('show');
                endToast('회의가 종료되었습니다.');
            }catch(e){
                console.error(e);
                endToast('업로드 또는 처리 중 오류가 발생했습니다.');
            }finally{
                startBtn.disabled = false;
                stopBtn.disabled  = true;
                root.classList.remove('rec-on');
            }
            // --- 👇 Agora 종료 로직 ---
            if (localAudioTrack) {
                localAudioTrack.close();
            }
            if (agoraClient) {
                await agoraClient.leave();
                console.log("👋 Agora 채널 퇴장.");
            }
        }

        async function enumerateMics(){
            try{
                await navigator.mediaDevices.getUserMedia({audio:true});
                const list = await navigator.mediaDevices.enumerateDevices();
                const mics = list.filter(d=>d.kind==='audioinput');
                if (micSel){
                    micSel.innerHTML = mics.map(d => `<option value="${d.deviceId}">${d.label||'마이크'}</option>`).join('') || '<option>기본 마이크</option>';
                }
            }catch(e){ /* ignore */ }
        }

        function s3Key(orgId, projectId, meetingId){
            const ts = Date.now();
            return `org/${orgId}/project/${projectId}/meeting/${meetingId}/${ts}.webm`;
        }
        async function uploadViaPresigned(blob, key){
            const qs = new URLSearchParams({ key, contentType: blob.type });
            const presigned = await api('GET', `${CFG().presignUrl}?${qs}`);
            const put = await fetch(presigned.url, { method:'PUT', headers: presigned.headers || {}, body: blob });
            if (!put.ok) throw new Error('S3 업로드 실패');
            return `${CFG().publicBaseUrl}/${key}`;
        }

        function openNotes(){ notesModal?.setAttribute('aria-hidden','false'); }
        function closeNotesFn(){ notesModal?.setAttribute('hidden','true'); }
        openBtn?.addEventListener('click', openNotes);
        closeNotes?.addEventListener('click', closeNotesFn);
        clearNotes?.addEventListener('click', ()=>{ if(notesBody) notesBody.textContent='(아직 내용이 없습니다)'; });
        exportNotes?.addEventListener('click', ()=>{
            const blob = new Blob([notesBody?.textContent||''], {type:'text/plain;charset=utf-8'});
            const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = 'meeting-notes.txt'; a.click();
            URL.revokeObjectURL(a.href);
        });

        function endToast(msg){ showEndToastAtHangup(msg, endBtn); }

        startBtn?.addEventListener('click', async (e)=>{
            e.preventDefault();
            const pid = getSelectedProjectId(root);
            if (pid){ await gateAccessForCurrentUser(root, pid); }
            if (document.getElementById('lobby-view')?.style.display === 'flex') return;
            await beginNotes();
        });
        stopBtn ?.addEventListener('click', ()=> endNotes());
        endBtn  ?.addEventListener('click', ()=> {
            if (!startBtn.disabled) { /* 작성 중 아님 */ } else { endNotes(); }
        });

        enumerateMics();
    }

    function showEndToastAtHangup(message, anchorBtn){
        let toast = document.getElementById('end-toast');
        if (!toast) {
            toast = document.createElement('div');
            toast.id = 'end-toast';
            toast.className = 'end-toast';
            document.body.appendChild(toast);
        }
        toast.textContent = message;

        toast.classList.add('anchored');
        toast.style.right = ''; toast.style.bottom = '';

        const btn = anchorBtn || document.querySelector('#hud-end') || document.querySelector('.hud-btn.danger');
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
            toast.classList.remove('anchored');
            toast.style.right = '24px'; toast.style.bottom = '24px';
            requestAnimationFrame(() => toast.classList.add('in'));
        }

        clearTimeout(showEndToastAtHangup._timer);
        showEndToastAtHangup._timer = setTimeout(() => {
            toast.classList.remove('in');
        }, 1800);
    }

    const SR = w.SpeechRecognition || w.webkitSpeechRecognition;
    const Speech = (function(){
        let recog=null, running=false;
        function ensure(){
            if (recog) return recog;
            if (!SR){ console.warn('이 브라우저는 음성 인식을 지원하지 않습니다.'); return null; }
            recog = new SR();
            recog.lang = 'ko-KR';
            recog.continuous = true;
            recog.interimResults = true;
            recog.onresult = (e)=>{
                let s=''; for (let i=e.resultIndex; i<e.results.length; i++) s += e.results[i][0].transcript;
                const body = document.getElementById('notes-body');
                if (body){
                    if (body.textContent==='(아직 내용이 없습니다)') body.textContent = s;
                    else body.textContent += s;
                }
            };
            recog.onend = ()=>{ running=false; };
            return recog;
        }
        return {
            start(){ const r = ensure(); if (!r || running) return; r.start(); running=true; },
            stop(){ if (!recog || !running) return; try{ recog.stop(); }catch(e){} running=false; }
        };
    })();

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

function setProjectUI(pid, root=document){
    const sel = root.querySelector('#projectSelect');
    if (!sel) return;
    if (pid==null){ sel.value = ''; return; }
    if (String(sel.value) !== String(pid)) sel.value = String(pid);
}

window.renderPeoplePanel = async function renderPeoplePanel(root=document, projectId){
    const listEl = root.querySelector('.rp-list'); if (!listEl) return;
    const cntEl  = root.querySelector('.rp-count, #rp-count');

    const me = await apiMe();
    const admin = isAdmin(me);

    const members = await apiListProjectMembers(projectId);

    const panel = root.querySelector('.room-people');
    if (panel) panel.classList.toggle('is-manager', admin);

    listEl.innerHTML = members.map(u => `
    <li class="person" data-id="${u.id}">
      <div class="avatar">${(u.name||'U')[0]}</div>
      <div class="p-main">
        <div class="p-name">
          ${u.name || ('유저#'+u.id)} ${u.role==='ADMIN' ? '<span class="role-badge">관리자</span>' : ''}
        </div>
        <div class="p-sub">온라인</div>
      </div>
      ${(admin && u.role!=='ADMIN') ? `<button class="chip" data-action="remove" data-id="${u.id}">제외</button>` : ''}
    </li>
  `).join('');
    if (cntEl) cntEl.textContent = String(members.length);

    if (panel && admin){
        listEl.onclick = async (e)=>{
            const btn = e.target.closest('.chip[data-action="remove"]'); if(!btn) return;
            const uid = Number(btn.dataset.id);
            await apiRemove(projectId, uid);
            renderPeoplePanel(root, projectId);
        };
    }
};

function initProjectSwitcher(root=document){
    const sel = root.querySelector('#projectSelect');
    if (sel){
        apiListProjects().then(list=>{
            const options = list.map(p => `<option value="${p.id}">${p.name}</option>`).join('');
            sel.innerHTML = '<option value="" disabled selected>프로젝트 선택</option>' + options;
        }).catch(err => {
            console.error('프로젝트 목록을 불러오지 못했습니다:', err);
            sel.innerHTML = '<option value="" disabled selected>목록 로딩 실패</option>';
        });
    }

    const initialId = getInitialProjectId();
    if (initialId){
        setTimeout(() => {
            setProjectUI(initialId, root);
            renderPeoplePanel(root, initialId);
            gateAccessForCurrentUser(root, initialId);
        }, 100);
    } else {
        setProjectUI(null, root);
        clearPeoplePanel(root);
        hideLobby(root);
    }

    sel?.addEventListener('change', (e)=>{
        const pid = Number(e.target.value);
        if (!pid) return;
        setProjectUI(pid, root);
        renderPeoplePanel(root, pid);
        gateAccessForCurrentUser(root, pid);

        const url = new URL(location.href);
        url.searchParams.set('project', String(pid));
        history.pushState({}, '', url);
    });
}

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

async function gateAccessForCurrentUser(root, projectId){
    const lobby = root.querySelector('#lobby-view'); if (!lobby) return;

    const me = await apiMe();
    const admin = isAdmin(me);

    const memberIds  = await apiListProjectMemberIds(projectId);
    const invitedIds = await apiListInvitations(projectId);
    const invited    = invitedIds.includes(Number(me.id));

    if (admin || memberIds.includes(Number(me.id))){
        lobby.style.display = 'none';
        return;
    }

    if (invited){
        openLobby(root, {
            title: '프로젝트 참여 수락',
            desc:  '관리자가 보낸 초대를 수락하면 이 프로젝트 회의실을 사용할 수 있어요.',
            acceptLabel: '수락', declineLabel: '닫기',
            onAccept: async ()=>{ await apiAccept(projectId, me.id); renderPeoplePanel(root, projectId); },
            onDecline: async ()=>{ await apiDecline(projectId, me.id); showCancelCard('참가 요청이 취소되었습니다.'); }
        });
    }else{
        openLobby(root, {
            title: '초대 필요',
            desc:  '회사 관리자에게 초대를 요청하세요.',
            acceptLabel: '확인', declineLabel: '닫기', acceptDisabled: true
        });
    }
}

window.getInitialProjectId   = getInitialProjectId;
window.getSelectedProjectId  = getSelectedProjectId;
window.gateAccessForCurrentUser = gateAccessForCurrentUser;
window.initProjectSwitcher   = initProjectSwitcher;

function openLobby(root, {
    title = '확인', desc = '', acceptLabel = '확인', declineLabel = '취소',
    onAccept = () => {}, onDecline = () => {}, acceptDisabled = false
} = {}) {
    const lobby = root.querySelector('#lobby-view');
    if (!lobby) return;

    const t  = lobby.querySelector('.lobby-title');
    const d  = lobby.querySelector('.lobby-desc');
    let ok   = lobby.querySelector('#btn-join');
    let no   = lobby.querySelector('#btn-decline');

    t.textContent = title;
    d.textContent = desc;

    const okNew = ok.cloneNode(true);
    const noNew = no.cloneNode(true);
    ok.replaceWith(okNew);
    no.replaceWith(noNew);
    ok = okNew;
    no = noNew;

    ok.textContent = acceptLabel;
    no.textContent = declineLabel;
    ok.disabled = !!acceptDisabled;

    ok.addEventListener('click', async () => {
        lobby.style.display = 'none';
        try { await onAccept(); } catch(e) {}
    });
    no.addEventListener('click', async () => {
        lobby.style.display = 'none';
        try { await onDecline(); } catch(e) {}
    });

    lobby.style.display = 'flex';
}

(function setupCancelCard(){
    function ensureCancelStyles() {
        if (document.getElementById('cancel-card-style')) return;
        const css = `
.cancel-state{
  display:grid; place-items:center;
  min-height:calc(100dvh - 150px);
}
.cancel-card{
  display:flex; align-items:center; gap:12px;
  padding:18px 22px;
  background:#fff;
  border:1px solid #e6ebf3;
  border-radius:16px;
  box-shadow:0 12px 28px rgba(28,39,71,.10);
  color:#334155; font-weight:600; letter-spacing:.2px;
  animation:cc-in .22s ease-out both;
}
@keyframes cc-in{
  from{ transform:translateY(6px); opacity:0; }
  to{   transform:translateY(0);   opacity:1; }
}`;
        const s = document.createElement('style');
        s.id = 'cancel-card-style';
        s.textContent = css;
        document.head.appendChild(s);
    }
    window.__ensureCancelStyles__ = ensureCancelStyles;
})();
window.showCancelCard = function showCancelCard(message = '입장이 취소되었습니다.') {
    (window.__ensureCancelStyles__ || function(){})();

    document.querySelectorAll('.cancel-layer').forEach(n => n.remove());

    const layer = document.createElement('div');
    layer.className = 'cancel-layer';

    const state = document.createElement('div');
    state.className = 'cancel-state';

    const card = document.createElement('div');
    card.className = 'cancel-card';

    const ico = document.createElement('div');
    Object.assign(ico.style, {
        display: 'grid', placeItems: 'center',
        width: '40px', height: '40px', borderRadius: '999px',
        background: 'linear-gradient(180deg,#fca5a5,#ef4444)',
        color: '#fff', fontSize: '18px',
        boxShadow: 'inset 0 0 0 1px rgba(255,255,255,.35)'
    });
    ico.textContent = '⛔';

    const txt = document.createElement('div');
    txt.textContent = message;

    card.appendChild(ico);
    card.appendChild(txt);
    state.appendChild(card);
    layer.appendChild(state);

    document.body.appendChild(layer);

    setTimeout(() => {
        layer.classList.add('hide');
        setTimeout(() => layer.remove(), 220);
    }, 1600);

    return layer;
};

document.addEventListener('click', (e) => {
    const declineBtn = e.target.closest('[data-action="decline"], .btn-decline, .btn-cancel, #btn-invite-decline');
    if (!declineBtn) return;
    showCancelCard?.('참가 요청이 취소되었습니다.');
});