// /js/meeting.js
(function (w) {
  'use strict';

  /* ===========================
   * 1) Styles
   * =========================== */
  function ensureRoomStyles() {
    if (document.getElementById('meeting-hud-styles')) return;
    const st = document.createElement('style');
    st.id = 'meeting-hud-styles';
    st.textContent = `
:root{
  --border:#e6ebf3; --muted:#5b6b83; --ink:#0f172a; --ink2:#1f2937; --ink3:#334155;
}

/* 상단 타이틀 */
.room-topbar{ padding:14px 4px 10px; display:flex; justify-content:flex-start; align-items:center; min-height:56px; }
.room-title{ font-size: clamp(22px, 2.0vw, 30px); line-height:1.25; font-weight:800; color:var(--ink2); letter-spacing:-.01em; text-align:left;  margin-left: 18px; }

/* 본문 그리드: 좌(무대) / 우(참가자) */
.room-body{
  display:grid; grid-template-columns:1fr 340px; gap:24px;
  align-items:stretch; min-height:835px; margin-top:6px;
}

/* 좌측 무대 카드 */
.room-stage{
  position:relative;
  background:#fff; border:1px solid var(--border); border-radius:16px;
  box-shadow:0 8px 20px rgba(28,39,71,.06);
  display:flex; align-items:center; justify-content:center;
  min-height:700px;
}
.stage-icon{ position:relative; width:220px; height:220px; display:flex; align-items:center; justify-content:center; }
.voice-mic{ width:96px; height:96px; position:relative; z-index:2; }
.ring{ position:absolute; left:50%; top:50%; width:180px; height:180px; border-radius:999px;
  transform:translate(-50%,-50%); background:radial-gradient(closest-side, rgba(124,58,237,.65), rgba(6,182,212,.38), transparent 70%);
  animation:ring-pulse 2.4s infinite ease-out; animation-play-state:paused; opacity:0; z-index:1; pointer-events:none;
}
.room-wrap.rec-on .ring{ opacity:1; animation-play-state:running; }
.ring.r2{ animation-delay:.9s; }
@keyframes ring-pulse{ 0%{transform:translate(-50%,-50%) scale(.82);opacity:0} 10%{opacity:.9} 100%{transform:translate(-50%,-50%) scale(1.25);opacity:0} }

/* 우측 참가자 카드 */
.room-people{
  background:#fff; border:1px solid var(--border); border-radius:16px;
  box-shadow:0 8px 20px rgba(28,39,71,.06);
  padding:16px 14px; display:flex; flex-direction:column;
}
.rp-head{ display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; }
.rp-title{ display:flex; align-items:center; gap:8px; font-weight:800; color:var(--ink); }
.rp-count{ margin-left:4px; background:#eef3ff; border-radius:999px; padding:2px 8px; font-size:12px; }
.rp-search{ height:32px; border:1px solid #dbe2ef; border-radius:10px; padding:0 10px; outline:none; font-size:13px; min-width:140px; }

/* 참가자 리스트 + 호버 액션 */
.room-people-list{ list-style:none; margin:0; padding:6px 2px 10px; display:flex; flex-direction:column; gap:12px; overflow:auto; flex: 1 1 auto;  }
.person{ display:flex; align-items:center; gap:12px; padding:6px 8px; border-radius:10px; transition:background .15s, box-shadow .15s; }
.person:hover{ background:#f8fafc; box-shadow:inset 0 0 0 1px #e5e7eb; }
.avatar{ width:42px; height:42px; border-radius:999px; display:grid; place-items:center; color:#fff; font-weight:800;
  box-shadow:inset 0 0 0 1px rgba(255,255,255,.35); background:linear-gradient(180deg,#94a3ff,#6366f1);
}

.room-people #btn-more{
  align-self: flex-end;            /* 카드 오른쪽 끝 */
  width: auto !important;          /* 꽉 찼던 너비 해제 */
  height: 32px;
  padding: 0 14px;
  margin: 10px 6px 6px 0;          /* 위치 미세조정 */
  border-radius: 999px;
  border: 1px solid #d9e1f2;
  background: linear-gradient(180deg,#f8fafc,#eef2ff);
  color: #334155;
  font-size: 13px;
  font-weight: 700;
  display: inline-flex; align-items: center; gap: 6px;
  box-shadow: 0 1px 0 rgba(255,255,255,.65) inset, 0 4px 12px rgba(2,8,23,.08);
  transition: filter .15s, box-shadow .2s, transform .05s;
  cursor: pointer;
}
.room-people #btn-more:hover{ filter: saturate(108%); box-shadow: 0 6px 16px rgba(2,8,23,.12); }
.room-people #btn-more:active{ transform: translateY(1px); }
.room-people #btn-more::after{ content:'›'; font-weight:800; margin-left:2px; transform:translateY(-.5px); }

.av-1{ background:linear-gradient(180deg,#60a5fa,#3b82f6); }
.av-2{ background:linear-gradient(180deg,#34d399,#10b981); }
.av-3{ background:linear-gradient(180deg,#f472b6,#ec4899); }
.av-4{ background:linear-gradient(180deg,#fbbf24,#f59e0b); }
.p-main{ flex:1 1 auto; min-width:0; }
.p-name{ font-weight:700; color:var(--ink2); letter-spacing:-.01em; }
.p-sub{ font-size:12px; color:#64748b; margin-top:2px; }
.p-actions{
  display:flex; gap:6px; margin-left:auto; justify-content:flex-end;
  min-width:76px; opacity:0; visibility:hidden; transform:translateX(6px);
  transition:opacity .15s, transform .15s; pointer-events:none;
}
.person:hover .p-actions, .person:focus-within .p-actions{ opacity:1; visibility:visible; transform:none; pointer-events:auto; }
.chip{ border:1px solid rgba(0,0,0,.08); background:#f8fafc; border-radius:10px; height:28px; padding:0 8px; display:inline-flex; align-items:center; gap:4px; cursor:pointer; }
.chip:hover{ filter:brightness(1.02); box-shadow:0 1px 4px rgba(0,0,0,.06); }
.chip:active{ transform:translateY(1px); }

/* 기존 하단 컨트롤은 숨김(HUD 사용) */
.controls{ display:none !important; }

/* 로비(수락/거절) */
#lobby-view{
  display: none;                 /* 기본은 숨김 */
  position: absolute;            /* .room-wrap 기준으로 덮음 */
  inset: 0;                      /* 상하좌우 0 */
  /* place-items: center;  ← grid 전용, 제거 */

  /* flex로 정중앙 배치 */
  display: none;                 /* ← 유지 */
  align-items: center;
  justify-content: center;

  background: rgba(15,23,42,.35);
  -webkit-backdrop-filter: blur(10px) saturate(140%);
  backdrop-filter: blur(10px) saturate(140%);
  z-index: 100;
  padding: 0 24px;               /* 화면이 좁을 때 카드 좌우 숨김 방지 */
}

/* 안전: 오버레이 기준 부모 */
.room-wrap{ position: relative; }

#lobby-view .lobby-card{
  width:440px; max-width:90vw; padding:26px 24px 20px; border-radius:20px;
  border:1px solid rgba(15,23,42,.08);
  background:linear-gradient(180deg, rgba(255,255,255,.94), rgba(250,251,255,.88));
  box-shadow:0 24px 60px rgba(6,12,31,.25), 0 1px 0 rgba(255,255,255,.75) inset;
  transition:transform .2s ease, box-shadow .2s ease;
}
.lobby-title{ font-size:20px; font-weight:800; letter-spacing:-.01em; color:#0f172a; margin:4px 0 10px; }
.lobby-desc{ color:#5b6b83; margin-bottom:18px; }
.lobby-actions{ display:flex; gap:10px; justify-content:center; padding-top:6px; }
#btn-join{
  background:linear-gradient(90deg,#4f8cff,#2f6bff); color:#fff; border:0; padding:12px 20px; border-radius:12px;
  box-shadow:0 10px 22px rgba(47,107,255,.28), inset 0 1px 0 rgba(255,255,255,.28);
}
#btn-decline{
  background:linear-gradient(180deg,#f8fafc,#eef2f7); color:#0f172a; border:1px solid rgba(15,23,42,.12);
  padding:12px 20px; border-radius:12px;
}

/* 회의 종료 토스트 (우하단 기본) */
.end-toast{
  position:fixed; right:24px; bottom:24px; z-index:9999;
  background:rgba(17,24,39,.92); color:#fff; padding:12px 16px; border-radius:12px;
  border:1px solid rgba(255,255,255,.08);
  box-shadow:0 14px 34px rgba(2,8,23,.30);
  backdrop-filter: blur(6px) saturate(130%);
  opacity:0; transform:translateY(8px); transition:opacity .2s, transform .2s;
}
.end-toast.in{ opacity:1; transform:translateY(0); }

/* 버튼 옆으로 앵커될 때(오른쪽에 딱 붙게) */
.end-toast.anchored{
  right:auto; bottom:auto; transform:none;
}
.end-toast.anchored::after{
  content:""; position:absolute; left:-8px; top:50%; transform:translateY(-50%);
  border-width:8px; border-style:solid; border-color:transparent #111827 transparent transparent;
}

/* 회의록 모달 */
.notes-modal{
  position:fixed; inset:0; display:none; align-items:center; justify-content:center;
  background:rgba(15,23,42,.40); backdrop-filter: blur(6px) saturate(130%); -webkit-backdrop-filter: blur(6px) saturate(130%);
  z-index:2000; box-sizing:border-box; padding: clamp(16px, 3vw, 28px);
}
.notes-modal[aria-hidden="false"]{ display:flex !important; }
@keyframes notes-pop{ from{ transform:translateY(8px) scale(.985); opacity:0; } to{ transform:none; opacity:1; } }
.notes-card{
  width:min(920px, 96vw); height:min(76vh, 760px); position:relative; display:flex; flex-direction:column; overflow:hidden; border-radius:18px;
  background: linear-gradient(180deg, rgba(255,255,255,.92), rgba(248,250,255,.86));
  backdrop-filter: blur(8px) saturate(140%); -webkit-backdrop-filter: blur(8px) saturate(140%);
  box-shadow:0 26px 80px rgba(2,8,23,.28), 0 2px 0 rgba(255,255,255,.60) inset; animation: notes-pop .18s ease both;
}
.notes-card::before{
  content:""; position:absolute; inset:0; border-radius:inherit; pointer-events:none; padding:1px;
  background: linear-gradient(135deg,#c7d2fe 0%,#93c5fd 40%,#a7f3d0 100%);
  -webkit-mask:linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0); -webkit-mask-composite: xor; mask-composite: exclude;
}
.notes-head{
  position:sticky; top:0; z-index:1; display:flex; align-items:center; justify-content:space-between;
  padding:14px 16px; background: linear-gradient(180deg, rgba(255,255,255,.96), rgba(250,252,255,.90));
  border-bottom:1px solid rgba(2,8,23,.06); backdrop-filter: blur(4px) saturate(120%);
}
.notes-head .title{ font-weight:800; letter-spacing:-.01em; color:#0f172a; }
.notes-head button, .notes-actions button{
  height:38px; padding:0 14px; border-radius:12px; border:1px solid rgba(2,8,23,.10);
  background:linear-gradient(180deg,#f8fafc,#eef2f7); color:#0f172a; font-weight:700; cursor:pointer;
}
.notes-body{ flex:1; overflow:auto; padding:16px 18px 20px; font-size:14.5px; line-height:1.68; color:#334155; white-space:pre-wrap; }
.notes-body::-webkit-scrollbar{ width:12px; }
.notes-body::-webkit-scrollbar-thumb{ background: linear-gradient(180deg,#c7d2fe,#a5b4fc); border-radius:999px; border:3px solid transparent; background-clip:content-box; }
.notes-actions{ display:flex; gap:10px; justify-content:flex-end; padding:12px 16px; border-top:1px solid rgba(2,8,23,.06);
  background: linear-gradient(180deg, rgba(255,255,255,.92), rgba(248,250,255,.88)); }
.notes-actions button:last-child{
  background:linear-gradient(90deg,#60a5fa,#3b82f6); color:#fff; border-color:#3b82f6;
  box-shadow:0 10px 22px rgba(59,130,246,.22), inset 0 0 0 1px rgba(255,255,255,.25);
}

/* HUD (디스코드풍) */
.stage-hud{ position:absolute; left:50%; bottom:18px; transform:translate(-50%,12px); opacity:0; pointer-events:none; transition:opacity .16s, transform .16s; }
.room-stage:hover .stage-hud, .stage-hud:hover{ opacity:1; transform:translate(-50%,0); pointer-events:auto; }
.hud-bar{
  display:flex; align-items:center; gap:12px;
  background:linear-gradient(180deg,#0b0f14 0%, #090d12 100%); color:#fff;
  border:1px solid rgba(255,255,255,.10); border-radius:14px;
  padding:10px 12px; box-shadow:0 12px 28px rgba(2,8,23,.40); backdrop-filter:blur(6px) saturate(125%);
}
.hud-left,.hud-center,.hud-right{ display:flex; align-items:center; gap:10px; }
.hud-left{ padding-right:8px; border-right:1px solid rgba(255,255,255,.10); }
.hud-right{ padding-left:8px; border-left:1px solid rgba(255,255,255,.10); }
.hud-btn{
  height:44px; padding:0 18px; border-radius:12px; border:1px solid rgba(255,255,255,.16);
  background:rgba(255,255,255,.08); color:#fff; cursor:pointer; font-size:15px; font-weight:700; white-space:nowrap; letter-spacing:-.01em;
}
.hud-btn:hover{ background:rgba(255,255,255,.16); }
.hud-btn.primary{ background:linear-gradient(90deg,#60a5fa,#3b82f6); border-color:#3b82f6; }
.hud-btn.danger{ background:#ef4444; border-color:#ef4444; }
.hud-btn:disabled{ opacity:.55; cursor:not-allowed; }
.hud-devsel{ height:44px; min-width:190px; padding:0 12px; border-radius:12px; border:1px solid rgba(255,255,255,.16); background:rgba(255,255,255,.08); color:#fff; white-space:nowrap; }

/* "회의록" 버튼은 기본 숨김, 작성 종료 후 등장 */
.hud-btn.notes{ width:0; padding:0; margin-left:0; opacity:0; overflow:hidden; transition:width .18s ease, padding .18s ease, margin .18s ease, opacity .18s ease; }
.hud-btn.notes.show{ width:auto; padding:0 18px; margin-left:8px; opacity:1; }

/* 거절 시 안내 */
.cancel-state{ display:grid; place-items:center; min-height:calc(100dvh - 150px); }
.cancel-card{
  display:flex; align-items:center; gap:12px; padding:18px 22px; background:#fff;
  border:1px solid #e6ebf3; border-radius:16px; box-shadow:0 12px 28px rgba(28,39,71,.10);
  color:#334155; font-weight:600; letter-spacing:.2px; animation:cc-in .22s ease-out both;
}
.cancel-card::before{
  content:"⛔"; display:grid; place-items:center; width:40px; height:40px; border-radius:999px;
  background:linear-gradient(180deg,#fca5a5,#ef4444); color:#fff; font-size:18px;
  box-shadow:inset 0 0 0 1px rgba(255,255,255,.35);
}

/* 화면을 완전히 덮는 오버레이 */
.cancel-layer{
  position: fixed;
  inset: 0;
  z-index: 2147483647; /* 최상단 */
  display: grid;
  place-items: center;
  /* 아주 옅은 흰 배경 + 약간의 블러 */
  background: rgba(248,250,252,.92);
  backdrop-filter: saturate(120%) blur(1px);
  animation: cancel-fade-in .12s ease-out both;
}

.cancel-layer.hide{
  animation: cancel-fade-out .18s ease-in both;
}

@keyframes cancel-fade-in{
  from{ opacity:0 }
  to  { opacity:1 }
}
@keyframes cancel-fade-out{
  to{ opacity:0; transform: translateY(4px); }
}

/* 프로젝트 선택 셀렉트: 제목처럼 보이게 */
.project-select{
  font-weight: 700;
  font-size: 20px;              /* ← 살짝 키움 (원하면 22px) */
  line-height: 1.2;
  color: var(--text, #111827);

  /* 보더/배경/그림자 모두 제거 = 깔끔한 텍스트형 셀렉트 */
  border: 0;
  background: transparent;
  box-shadow: none;

  padding: 2px 28px 2px 2px;    /* 오른쪽 화살표 공간만 확보 */
  border-radius: 0;
  min-width: 140px;

  appearance:none; -webkit-appearance:none; -moz-appearance:none;
  background-image: url("data:image/svg+xml;utf8,\
  <svg xmlns='http://www.w3.org/2000/svg' width='18' height='18' viewBox='0 0 24 24' fill='none' stroke='%235b6b83' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><polyline points='6 9 12 15 18 9'/></svg>");
  background-repeat: no-repeat;
  background-position: right 6px center;
  background-size: 16px 16px;
}

.project-select:hover{
  border: 0;
  box-shadow: none;
  background: transparent;
}

.project-select:focus{
  outline: none;        /* 보더 없이 포커스 처리 */
  border: 0;
  box-shadow: none;     /* 필요하면 아주 얕은 포커스 링으로 바꿔도 됨 */
  background: transparent;
}

/* 펼친 목록의 타이포만 살짝 정리(브라우저 한계 내에서) */
.project-select option{
  font-weight: 600;
  font-size: 16px;
  background: #fff;
  color: #111827;
}
.project-select option:checked{
  background: #eef2ff;   /* 선택 항목 하이라이트 */
  color: #1f2937;
}

.room-title #projectTitle{ display:none; }

/* 참가자 패널: 관리자 뱃지/초대 버튼 */
.role-badge{
  margin-left:6px; padding:2px 6px; border-radius:999px;
  font-size:12px; font-weight:600;
  background:#eef2ff; color:#374151;
}
.person .invite-btn{
  margin-left:auto; padding:6px 10px;
  border:1px solid var(--border,#e6ebf3);
  border-radius:8px; background:#fff; color:#1f2937; cursor:pointer;
}
.person .invite-btn:hover{ background:#f8fafc; }
.person .invite-btn.remove{ border-color:#fecaca; color:#991b1b; }
.list-sep{ margin:10px 0 6px; padding:0 4px; font-size:12px; color:#6b7280; }

/* 관리자만 검색바 노출(마크업에 .rp-search가 있을 때) */
.room-people .rp-search{ display:none; }
.room-people.is-manager .rp-search{ display:block; }

@keyframes cc-in{ from{ transform:translateY(6px); opacity:0; } to{ transform:translateY(0); opacity:1; } }
`;
    document.head.appendChild(st);
  }

  /* ===========================
   * 2) HTML
   * =========================== */
  function roomHTML() {
    return `
<section class="room-wrap">

  <!-- 로비(수락/거절) -->
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
  <select id="projectSelect" class="project-select" aria-label="프로젝트 선택">
    <option value="1">프로젝트 1</option>
    <option value="2">프로젝트 2</option>
    <option value="3">프로젝트 3</option>
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

      <!-- HUD (호버시 표시) -->
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
        <div class="rp-title">
          <span class="rp-ic">👥</span>
          <span>참가자</span>
          <span class="rp-count" id="rp-count">0</span>
        </div>
      <input class="rp-search" type="text" placeholder="검색" />
    </div>

    <!-- ★ JS가 채울 컨테이너: 비워두세요 -->
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
    </section>`;
  }

  /* ===========================
   * 3) Events & Logic
   * =========================== */
  function bindRoomEvents(root){
    // HUD
    const startBtn = root.querySelector('#hud-notes-start');
    const stopBtn  = root.querySelector('#hud-notes-stop');
    const openBtn  = root.querySelector('#hud-open-notes');
    const endBtn   = root.querySelector('#hud-end');
    const micSel   = root.querySelector('#hud-mic');

    // 모달
    const notesModal  = document.getElementById('notes-modal');
    const notesBody   = document.getElementById('notes-body');
    const closeNotes  = document.getElementById('btn-close-notes');
    const clearNotes  = document.getElementById('btn-clear-notes');
    const exportNotes = document.getElementById('btn-export-notes');

    function openNotes(){
      notesModal.setAttribute('aria-hidden','false');
    }
    function closeNotesFn(){
      notesModal.setAttribute('aria-hidden','true');
    }

    openBtn?.addEventListener('click', openNotes);
    closeNotes?.addEventListener('click', closeNotesFn);
    clearNotes?.addEventListener('click', ()=> notesBody.textContent = '(아직 내용이 없습니다)');
    exportNotes?.addEventListener('click', ()=>{
      const blob = new Blob([notesBody.textContent||''], {type:'text/plain;charset=utf-8'});
      const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = 'meeting-notes.txt'; a.click();
      URL.revokeObjectURL(a.href);
    });

    // 회의록 작성 시작/종료
  function beginNotes(){
    startBtn.disabled = true; stopBtn.disabled = false;
    openBtn.classList.remove('show'); // 작성 중에는 숨김
    Speech.start();
    root.classList.add('rec-on');     // 네온 링 ON
  }
  function endNotes(){
    startBtn.disabled = false; stopBtn.disabled = true;
    Speech.stop();
    root.classList.remove('rec-on');  // 네온 링 OFF
    requestAnimationFrame(()=> openBtn.classList.add('show')); // 회의록 버튼 등장
  }

  // 회의록 시작
  startBtn?.addEventListener('click', async (e) => {
    e.preventDefault();

  // (선택) 프로젝트 멤버십 가드 — 멤버 아니면 프로젝트 참여 수락 모달 열고 종료
  const pid = getCurrentProjectId?.(); // 너희가 쓰는 선택값 반환 함수
  if (pid) {
    const ids = await apiListProjectMemberIds(pid);
    const isAdmin = currentUserId === COMPANY_ADMIN_ID;
    if (!isAdmin && !ids.includes(currentUserId)) {
      gateAccessForCurrentUser(document, pid);
      return;
    }
  }

  // 진짜 시작
  beginNotes();
  (document.querySelector('.room-wrap') || document).classList.add('in-call');
  });

  stopBtn ?.addEventListener('click', endNotes);

  // 통화 종료 → 버튼 오른쪽 토스트
  endBtn?.addEventListener('click', () => {
    if (!startBtn.disabled) { /* 작성 중 아님 */ } else { endNotes(); }
      showEndToastAtHangup('회의가 종료되었습니다.', endBtn);
    });

  // 마이크 리스트(가능하면 채우기)
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

  // 통화 종료 토스트 (버튼 오른쪽에 앵커)
  function showEndToastAtHangup(message, anchorBtn){
    let toast = document.getElementById('end-toast');
    if (!toast) {
      toast = document.createElement('div');
      toast.id = 'end-toast';
      toast.className = 'end-toast';
      document.body.appendChild(toast);
    }
    toast.textContent = message;

    // 앵커 모드
    toast.classList.add('anchored');
    toast.style.right = ''; toast.style.bottom = '';

    // 버튼 기준 위치 계산 (fixed 좌표계 = viewport 기준)
    const btn = anchorBtn || document.querySelector('#hud-end') || document.querySelector('.hud-btn.danger');
    if (btn) {
      const r = btn.getBoundingClientRect();
      // 먼저 임시로 보이게 해서 높이 측정
      toast.style.left = (r.right + 28) + 'px';
      toast.style.top  = r.top + 'px';
      requestAnimationFrame(() => {
        const h = toast.offsetHeight || 40;
        toast.style.top = Math.round(r.top + (r.height - h) / 2) + 'px';
        toast.classList.add('in');
      });
    } else {
      // 폴백: 우하단
      toast.classList.remove('anchored');
      toast.style.right = '24px'; toast.style.bottom = '24px';
      requestAnimationFrame(() => toast.classList.add('in'));
    }

    clearTimeout(showEndToastAtHangup._timer);
    showEndToastAtHangup._timer = setTimeout(() => {
      toast.classList.remove('in');
    }, 1800);
  }

  /* ===========================
   * 4) Speech (Web Speech API – 라이트)
   * =========================== */
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

  /* ===========================
   * 5) Public API
   * =========================== */
  const Meeting = {
  mount(target){
    const el = (typeof target === 'string') ? document.querySelector(target) : target;
    if (!el) return;
    ensureRoomStyles();
    el.innerHTML = roomHTML();

    const wrap = el.querySelector('.room-wrap');
    bindRoomEvents(wrap);

    // ★ 프로젝트 스위처 + 참가자 패널 초기화
    initProjectSwitcher(wrap);
  }
  };
  w.Meeting = Meeting;
  })(window);

  // ─────────────────────────────────────────────────────────────
  // [DEV ONLY] 로그인 사용자 (우리는 일반회원)
  const currentUserId = 1; // 이재환
  // TODO: [API] GET /api/me → { id, name, role: 'admin' | 'member', companyId }

  const USERS = [
    { id: 1, name: '이재환' },
    { id: 2, name: '박소현' },
    { id: 3, name: '김형욱' },
    { id: 4, name: '양수빈' },
    { id: 5, name: '이민우' },
  ];
  // TODO: [API] GET /api/users?companyId=...

  // ★ 회사(테넌트) 관리자 — 회원가입 시 '관리자'로 가입한 1명
  const COMPANY_ADMIN_ID = 2; // 박소현(예시)
  // TODO: [API] GET /api/company/:id → {adminId}

  // 프로젝트들(관리자는 공통이므로 별도 필드 없이 members만 둠)
  const PROJECTS = [
    { id: 1, name: '프로젝트 1', members: [3,5] },  // 지금 우리는 미초대 상태 예시
    { id: 2, name: '프로젝트 2', members: [1,3] },    // 여기엔 우리가 초대되어 있음 예시
    { id: 3, name: '프로젝트 3', members: [] },
  ];
  // TODO: [API] GET /api/projects?companyId=...

  // 초대함(프로젝트별 대기중 초대)
  // key: projectId, value: userId[]
  const INVITATIONS = {
    1: [1],  // 우리는 프로젝트1에 '초대받은 상태'
    2: [],   // 이미 멤버
    3: [],   // 미초대
  };
  // TODO: [API] GET /api/projects/:id/invitations

  // 로컬 상태(백엔드 붙이면 삭제 가능)
  const state = { users: USERS, projects: PROJECTS, invitations: INVITATIONS };

  // ── API 스텁 (지금은 로컬 state 사용, 나중에 fetch로 대체) ──────────
  async function apiListProjects(){ return state.projects; }      // TODO: [API]
  async function apiListUsers(){ return state.users; }            // TODO: [API]
  // 멤버 = 관리자 + 해당 프로젝트 members (초대/현재유저 끼워넣지 않음)
  async function apiListProjectMemberIds(projectId){
    const p = state.projects.find(x => x.id === Number(projectId));
    const base = (p && Array.isArray(p.members)) ? p.members : [];
  // ★ 현재 유저, 초대 등은 절대 끼우지 않음
  return Array.from(new Set([COMPANY_ADMIN_ID, ...base]));
  } // TODO: [API] GET /api/projects/:id/members (+ admin merge on server)

  // 관리자 + 프로젝트 members만 반환 (현재 유저/초대 등 절대 끼우지 않음)
  async function apiListProjectMembers(projectId){
    const ids   = await apiListProjectMemberIds(projectId); // 이미 관리자 포함되어 옴
    const users = await apiListUsers();
    const uniq  = Array.from(new Set(ids));
    return users.filter(u => uniq.includes(u.id));
  }

  async function apiListInvitations(projectId){
    return state.invitations[projectId] || [];
  } // TODO: [API] GET /api/projects/:id/invitations

  async function apiInvite(projectId, userId){
    // 관리자만 가능: 서버에서 검증 예정
    state.invitations[projectId] = Array.from(new Set([...(state.invitations[projectId]||[]), Number(userId)]));
    return { ok:true };
  } // TODO: [API] POST /api/projects/:id/invitations

  async function apiAccept(projectId, userId){
    // 초대 수락 → 멤버 편입
    const p = state.projects.find(x => x.id === Number(projectId));
    if (p){
      p.members = Array.from(new Set([...(p.members||[]), Number(userId)]));
    }
    state.invitations[projectId] = (state.invitations[projectId]||[]).filter(id => id!==Number(userId));
    return { ok:true };
  } // TODO: [API] POST /api/projects/:id/members (from invitation)

  async function apiDecline(projectId, userId){
    state.invitations[projectId] = (state.invitations[projectId]||[]).filter(id => id!==Number(userId));
    return { ok:true };
  } // TODO: [API] DELETE /api/projects/:id/invitations/:userId

  async function apiRemove(projectId, userId){
    const p = state.projects.find(x => x.id === Number(projectId));
    if (p){
      p.members = (p.members||[]).filter(id => id !== Number(userId));
    }
    return { ok:true };
  } // TODO: [API] DELETE /api/projects/:id/members/:userId

  // URL/Router에서 초기 프로젝트 결정
  function getInitialProjectId() {
  const q = new URLSearchParams(location.search);
  return q.has('project') ? Number(q.get('project')) : null;
  }
  

  // 셀렉트 박스 동기화
  function setProjectUI(pid, root = document) {
    const sel = root.querySelector('#projectSelect');
    const exists = state.projects.find(p => p.id === Number(pid));
    if (sel && exists && sel.value !== String(pid)) sel.value = String(pid);
  }

  // 참가자 패널 렌더: "관리자 + 실제 멤버"만 표시 (현재 유저를 임의로 끼우지 않음)
  window.renderPeoplePanel = async function renderPeoplePanel(root = document, projectId){
    const listEl = root.querySelector('.rp-list');                 // 회의실 DOM이 아닐 수 있으니
    const cntEl  = root.querySelector('.rp-count, #rp-count');     // 없으면 바로 종료
    if (!listEl) return;

    // 1) 멤버 아이디는 "회사 관리자 + 프로젝트 members"만
    const [ids, users] = await Promise.all([
      apiListProjectMemberIds(projectId),  // => [2,3,5] (예시)
      apiListUsers()
    ]);
    // 혹시라도 중복 제거
    const memberIds = Array.from(new Set(ids));
    const members   = users.filter(u => memberIds.includes(u.id));

    // (선택) 관리자 UI 토글
    const panel = root.querySelector('.room-people');
    if (panel) panel.classList.toggle('is-manager', currentUserId === COMPANY_ADMIN_ID);

    // 2) 목록 렌더 (현재 유저라고 해서 따로 끼워 넣지 않음)
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

  // 3) (관리자일 때만) 초대 섹션
  if (currentUserId === COMPANY_ADMIN_ID){
    const candidates = users.filter(u => u.id !== COMPANY_ADMIN_ID && !memberIds.includes(u.id));
    if (candidates.length){
      listEl.insertAdjacentHTML('beforeend', `
        <li class="list-sep">초대 가능</li>
        ${candidates.map(u => `
          <li class="person candidate" data-id="${u.id}">
            <div class="avatar muted">${u.name[0]}</div>
            <div class="p-main">
              <div class="p-name">${u.name}</div>
              <div class="p-sub muted">오프라인</div>
            </div>
            <button class="chip" data-action="invite" data-id="${u.id}">초대</button>
          </li>
        `).join('')}
      `);
    }

    // 초대/제외 버튼 핸들러
    listEl.onclick = async (e) => {
      const btn = e.target.closest('.chip');
      if (!btn) return;
      const uid = Number(btn.dataset.id);
      const action = btn.dataset.action;
      if (action === 'invite')      await apiInvite(projectId, uid);
      else if (action === 'remove') await apiRemove(projectId, uid);
      renderPeoplePanel(root, projectId); // 갱신
    };
  }
};
  
// === [UPDATE] 프로젝트 스위처 초기화 ===
function initProjectSwitcher(root = document){
  const sel = root.querySelector('#projectSelect');

  // (옵션) 서버에서 옵션 채우기
  // apiListProjects().then(list => {
  //   sel.innerHTML = list.map(p => `<option value="${p.id}">${p.name}</option>`).join('');
  // });

  // URL 파라미터로 들어온 경우만 즉시 세팅, 그 외엔 '프로젝트 선택' 상태 유지
  const initialId = getInitialProjectId();
  if (initialId){
    setProjectUI(initialId, root);
    renderPeoplePanel(root, initialId);
    gateAccessForCurrentUser(root, initialId); // 선택된 경우에만 게이트 체크
  }else{
    // 아무것도 선택 안 한 초기 상태
    setProjectUI(null, root);        // 제목을 '프로젝트 선택' 등으로 표시하고
    clearPeoplePanel(root);          // 우측 참가자 목록 비우고
    hideLobby(root);                 // 초대 모달/로비는 숨김
  }

  if (!sel) return;
  sel.addEventListener('change', (e) => {
    const pid = Number(e.target.value);
    setProjectUI(pid, root);
    renderPeoplePanel(root, pid);
    gateAccessForCurrentUser(root, pid); // ▶ 여기서만 프로젝트 참여 수락 모달을 검사/노출

    // URL 업데이트(라우터 없으면 쿼리만)
    if (window.Router && typeof Router.go === 'function') {
      Router.go('meeting', { projectId: pid });
    } else {
      const url = new URL(location.href);
      url.searchParams.set('project', String(pid));
      history.pushState({}, '', url);
    }
  });
}

// 보조들(없으면 추가)
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

  function resetStubStore(){
  // 원본 상수로 되감기
  state.projects    = JSON.parse(JSON.stringify(PROJECTS));
  state.invitations = JSON.parse(JSON.stringify(INVITATIONS));
}

window.resetStubStore = function(){
  state.projects = [
    { id: 1, name: '프로젝트 1', members: [3, 5] }, // 우리는 멤버 아님
    { id: 2, name: '프로젝트 2', members: [1, 3] }, // 우리는 멤버
    { id: 3, name: '프로젝트 3', members: [] }
  ];
  state.invitations = { 1: [1], 2: [], 3: [] };     // 프로젝트1은 '초대 대기' 상태
};

function resetMeetingUI(root = document){
  const lobby  = root.querySelector('#lobby-view');
  const listEl = root.querySelector('.rp-list, .room-people-list')
  if (lobby)  lobby.style.display = 'none'; 
  if (listEl) listEl.innerHTML = '';
}

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

  // 텍스트/상태
  t.textContent = title;
  d.textContent = desc;

  // 🔁 버튼을 "클론 → 교체"해서 과거 핸들러 싹 제거
  const okNew = ok.cloneNode(true);
  const noNew = no.cloneNode(true);
  ok.replaceWith(okNew);
  no.replaceWith(noNew);
  ok = okNew;
  no = noNew;

  ok.textContent = acceptLabel;
  no.textContent = declineLabel;
  ok.disabled = !!acceptDisabled;

  // 새 핸들러 연결
  ok.addEventListener('click', async () => {
    lobby.style.display = 'none';
    try { await onAccept(); } catch(e) {}
  });
  no.addEventListener('click', async () => {
     // ⬇️ 먼저 로비를 숨겨 카드가 위에 보이도록
   lobby.style.display = 'none';
   try { await onDecline(); } catch(e) {}
  });

  // 표시
  lobby.style.display = 'flex';
}
  // 초대 게이트: 멤버가 아니고 초대받은 사람에게만 수락/거절 표시
  async function gateAccessForCurrentUser(root, projectId){
  const lobby = root.querySelector('#lobby-view');
  if (!lobby) return;

  // 회사 단일 관리자 구조 (COMPANY_ADMIN_ID)
  const memberIds  = await apiListProjectMemberIds(projectId);
  const invitedIds = await apiListInvitations(projectId);
  const isAdmin    = (currentUserId === COMPANY_ADMIN_ID);
  const invited    = invitedIds.includes(currentUserId);  // ← 선언을 위로!

  // 1) 관리자이거나 이미 멤버면 로비 숨김
  if (isAdmin || memberIds.includes(currentUserId)) {
    lobby.style.display = 'none';
    return;
  }

  // 2) 초대 받은 경우 → "프로젝트 참여 수락" 모달
if (invited) {
  openLobby(root, {
    title: '프로젝트 참여 수락',
    desc:  '관리자가 보낸 초대를 수락하면 이 프로젝트 회의실을 사용할 수 있어요.',
    acceptLabel: '수락',
    declineLabel: '닫기',
    acceptDisabled: false, // ← 버튼 비활성화 풀기
    onAccept: async () => {
      await apiAccept(projectId, currentUserId);   // 멤버 편입
      renderPeoplePanel(root, projectId);          // 우측 패널 갱신
    },
    onDecline: async () => {
      await apiDecline(projectId, currentUserId);  // 초대 제거
      showCancelCard('참가 요청이 취소되었습니다.');
    }
  });
} else {
  // 3) 초대도 아닌 경우 → "초대 필요"
  openLobby(root, {
    title: '초대 필요',
    desc:  '회사 관리자에게 초대를 요청하세요.',
    acceptLabel: '확인',
    declineLabel: '닫기',
    acceptDisabled: true
  });
}
  }
function confirmJoinMeeting(root){
  openLobby(root, {
    title: '회의실 입장',
    desc:  '이 회의에 지금 입장하시겠습니까?',
    acceptLabel: '입장',
    declineLabel: '취소',
    onAccept: () => {
      // 기존 입장 처리 (예: beginNotes(); root.classList.add('in-call'); 등)
    },
    onDecline: () => {
      // ✅ 취소 토스트
      showCancelCard('입장이 취소되었습니다.');
      // (선택) 홈으로 돌려보내고 싶으면 아래 한 줄 추가
      // setTimeout(() => location.href = (window.APP_CTX || '') + '/mainbar', 800);
    }
  });
}

// 전역 노출(기존 호출부 호환)
window.confirmJoinMeeting = confirmJoinMeeting;

// Dev helpers (콘솔에서 사용)
window.Dev = {
  // 초대 필요 상태로 초기화
  reset(pid){
    const p = state.projects.find(x => x.id === Number(pid));
    if (p){
      p.members = (p.members || []).filter(id => id !== currentUserId);
    }
    state.invitations[pid] = (state.invitations[pid] || []).filter(id => id !== currentUserId);
    const wrap = document.querySelector('.room-wrap');
    renderPeoplePanel(wrap, pid);
    gateAccessForCurrentUser(wrap, pid);
  },
  // 관리자 초대 시뮬레이션
  inviteSelf(pid){
    state.invitations[pid] = Array.from(new Set([...(state.invitations[pid]||[]), currentUserId]));
    const wrap = document.querySelector('.room-wrap');
    gateAccessForCurrentUser(wrap, pid);
  }
};

// === [공통] 취소 상태 카드 스타일 주입 IIFE ===
(function setupCancelCard(){
  function ensureCancelStyles() {
    if (document.getElementById('cancel-card-style')) return;
    const css = `
/* === 거절 시 빈 화면 안내 (polish) === */
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
// 전역에서 부를 수 있게 노출
  window.__ensureCancelStyles__ = ensureCancelStyles;
})();
// 아이콘은 ::before 대신 실제 엘리먼트로 만들어 준다.
window.showCancelCard = function showCancelCard(message = '입장이 취소되었습니다.') {
  (window.__ensureCancelStyles__ || function(){})();
  
  // 중복 방지: 기존 레이어 제거
  document.querySelectorAll('.cancel-layer').forEach(n => n.remove());

  // ① 전체화면 오버레이
  const layer = document.createElement('div');
  layer.className = 'cancel-layer';

  // ② 가운데 카드 영역(기존 스타일 재사용)
  const state = document.createElement('div');
  state.className = 'cancel-state';

  const card = document.createElement('div');
  card.className = 'cancel-card';

  // 아이콘(실제 엘리먼트)
  const ico = document.createElement('div');
  Object.assign(ico.style, {
    display: 'grid',
    placeItems: 'center',
    width: '40px', height: '40px',
    borderRadius: '999px',
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

  // ★ body 최상단에 붙인다(내부 컨테이너 사용 X)
  document.body.appendChild(layer);

  // 1.6초 뒤 페이드아웃 후 제거
  setTimeout(() => {
    layer.classList.add('hide');
    setTimeout(() => layer.remove(), 220);
  }, 1600);

  return layer;
};

// (이 아래 이벤트 위임 코드는 그대로 사용해도 OK)
document.addEventListener('click', (e) => {
  const declineBtn = e.target.closest('[data-action="decline"], .btn-decline, .btn-cancel, #btn-invite-decline');
  if (!declineBtn) return;
  showCancelCard?.('참가 요청이 취소되었습니다.');
});

  