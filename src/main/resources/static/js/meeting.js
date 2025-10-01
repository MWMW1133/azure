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
  --hud-bg:#0b0f14; --hud-bg-2:#0e141b;
  --hud-stroke:rgba(255,255,255,.06);
  --hud-shadow:0 18px 40px rgba(3,10,20,.35);
  --ink:#dbe3f1;
}

/* 상단 타이틀 */
.room-topbar{ padding:14px 4px 10px; }
.room-title{ font-weight:700; color:var(--ink2); letter-spacing:-.01em; }

/* 본문 그리드: 좌(무대) / 우(참가자) */
.room-body{
  display:grid; grid-template-columns:1fr 340px; gap:24px;
  align-items:stretch; min-height:840px; margin-top:4px;
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
.room-people-list{ list-style:none; margin:0; padding:6px 2px 10px; display:flex; flex-direction:column; gap:12px; overflow:auto; }
.person{ display:flex; align-items:center; gap:12px; padding:6px 8px; border-radius:10px; transition:background .15s, box-shadow .15s; }
.person:hover{ background:#f8fafc; box-shadow:inset 0 0 0 1px #e5e7eb; }
.avatar{ width:42px; height:42px; border-radius:999px; display:grid; place-items:center; color:#fff; font-weight:800;
  box-shadow:inset 0 0 0 1px rgba(255,255,255,.35); background:linear-gradient(180deg,#94a3ff,#6366f1);
}
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

/* 기존 하단 컨트롤은 숨김(기능은 HUD로 이관) */
.controls{ display:none !important; }

/* 로비 */
.room-wrap{ position:relative; display:flex; flex-direction:column; min-height:calc(100vh - 180px); }

#lobby-view{
  position:absolute; inset:0;
  display:grid; place-items:center;
  background:rgba(15,23,42,.35);
  -webkit-backdrop-filter: blur(10px) saturate(140%);
  backdrop-filter: blur(10px) saturate(140%);
  z-index:100;
}

/* 카드: 은은한 유리 느낌 + 깊은 그림자 */
#lobby-view .lobby-card{
  width:440px; max-width:90vw;
  padding:26px 24px 20px;
  border-radius:20px;
  border:1px solid rgba(15,23,42,.08);
  background:linear-gradient(180deg, rgba(255,255,255,.94), rgba(250,251,255,.88));
  box-shadow:
    0 24px 60px rgba(6,12,31,.25),
    0 1px 0 rgba(255,255,255,.75) inset;
  transition:transform .2s ease, box-shadow .2s ease;
}
#lobby-view .lobby-card:hover{
  transform:translateY(-1px);
  box-shadow:
    0 28px 70px rgba(6,12,31,.28),
    0 1px 0 rgba(255,255,255,.78) inset;
}

.lobby-title{
  font-size:20px; font-weight:800; letter-spacing:-.01em;
  color:#0f172a;
  margin:4px 0 10px;
}
.lobby-desc{
  color:#5b6b83;
  margin-bottom:18px;
}

/* 버튼 영역 */
.lobby-actions{ display:flex; gap:10px; justify-content:center; padding-top:6px; }

/* 회의 종료 토스트 */
.end-toast{
  position:fixed; right:24px; bottom:24px; z-index:9999;
  background:#111827; color:#fff; padding:12px 16px; border-radius:12px;
  box-shadow:0 10px 24px rgba(0,0,0,.2); opacity:0; transform:translateY(8px);
  transition:opacity .2s, transform .2s;
}
.end-toast.in{ opacity:1; transform:translateY(0); }

/* 회의록 모달 */
.notes-modal{ position:fixed; inset:0; display:none; align-items:center; justify-content:center; background:rgba(15,23,42,.35); backdrop-filter:blur(2px); z-index:2000; }
.notes-modal[aria-hidden="false"]{ display:flex; }
.notes-card{ width:720px; max-width:92vw; height:min(72vh,720px); background:#fff; border:1px solid var(--border); border-radius:16px; box-shadow:0 20px 50px rgba(28,39,71,.25); display:flex; flex-direction:column; }
.notes-head{ display:flex; justify-content:space-between; align-items:center; padding:12px 14px; border-bottom:1px solid var(--border); }
.notes-body{ flex:1; overflow:auto; padding:14px; font-size:14px; white-space:pre-wrap; line-height:1.6; color:var(--ink3); }
.notes-actions{ padding:12px 14px; border-top:1px solid var(--border); display:flex; gap:8px; justify-content:flex-end; }

/* Discord-like HUD (큰 버튼, 2줄 방지) */
.stage-hud{
  position:absolute; left:50%; bottom:18px; transform:translate(-50%,12px);
  opacity:0; pointer-events:none; transition:opacity .16s, transform .16s;
}
.room-stage:hover .stage-hud, .stage-hud:hover{ opacity:1; transform:translate(-50%,0); pointer-events:auto; }
.hud-bar{
    display:flex; align-items:center; gap:12px;
    /* 더 진한 블랙 + 살짝 그라데이션 */
    background:linear-gradient(180deg,#0b0f14 0%, #090d12 100%); color:#fff;
    border:1px solid rgba(255,255,255,.10); border-radius:14px;
    padding:10px 12px; box-shadow:0 12px 28px rgba(2,8,23,.40); backdrop-filter:blur(6px) saturate(125%);
}
.hud-left,.hud-center,.hud-right{ display:flex; align-items:center; gap:10px; }
.hud-left{ padding-right:8px; border-right:1px solid rgba(255,255,255,.10); }
.hud-right{ padding-left:8px; border-left:1px solid rgba(255,255,255,.10); }
.hud-btn{
  height:44px; padding:0 18px; border-radius:12px; border:1px solid rgba(255,255,255,.16);
  background:rgba(255,255,255,.08); color:#fff; cursor:pointer; font-size:15px; font-weight:700;
  white-space:nowrap; letter-spacing:-.01em;
}
.hud-btn:hover{ background:rgba(255,255,255,.16); }
.hud-btn.primary{ background:linear-gradient(90deg,#60a5fa,#3b82f6); border-color:#3b82f6; }
.hud-btn.danger{ background:#ef4444; border-color:#ef4444; }
.hud-btn:disabled{ opacity:.55; cursor:not-allowed; }
.hud-devsel{
  height:44px; min-width:190px; padding:0 12px; border-radius:12px; border:1px solid rgba(255,255,255,.16);
  background:rgba(255,255,255,.08); color:#fff; white-space:nowrap;
}

/* "회의록" 버튼은 기본 숨김, 작성 종료 후 공간 열리며 등장 */
.hud-btn.notes{
  width:0; padding:0; margin-left:0; opacity:0; overflow:hidden;
  transition:width .18s ease, padding .18s ease, margin .18s ease, opacity .18s ease;
}
.hud-btn.notes.show{
  width:auto; padding:0 18px; margin-left:8px; opacity:1;
}

/* 수락: 블루 그라데이션 + 입체감 */
#btn-join{
  background:linear-gradient(90deg,#4f8cff,#2f6bff);
  color:#fff;
  border:0;
  padding:12px 20px;
  border-radius:12px;
  box-shadow:
    0 10px 22px rgba(47,107,255,.28),
    inset 0 1px 0 rgba(255,255,255,.28);
  transition:filter .15s ease, box-shadow .15s ease, transform .05s ease;
}
#btn-join:hover{
  filter:saturate(112%);
  box-shadow:
    0 16px 28px rgba(47,107,255,.36),
    inset 0 1px 0 rgba(255,255,255,.30);
  transform:translateY(-1px);
}
#btn-join:active{ transform:translateY(0); }

/* 거절: 밝은 고스트 톤 */
#btn-decline{
  background:linear-gradient(180deg,#f8fafc,#eef2f7);
  color:#0f172a;
  border:1px solid rgba(15,23,42,.12);
  padding:12px 20px;
  border-radius:12px;
  transition:filter .15s ease, box-shadow .15s ease, transform .05s ease;
}
#btn-decline:hover{
  filter:saturate(106%);
  box-shadow:0 8px 18px rgba(2,8,23,.12);
  transform:translateY(-1px);
}
#btn-decline:active{ transform:translateY(0); }

/* 접근성 포커스 링 */
#btn-join:focus-visible, #btn-decline:focus-visible{
  outline:none;
  box-shadow:
    0 0 0 3px rgba(59,130,246,.35),
    0 0 0 6px rgba(255,255,255,.9);
}

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

/* 아이콘은 CSS로만(마크업 추가 없음) */
.cancel-card::before{
  content:"⛔";
  display:grid; place-items:center;
  width:40px; height:40px; border-radius:999px;
  background:linear-gradient(180deg,#fca5a5,#ef4444);
  color:#fff; font-size:18px;
  box-shadow:inset 0 0 0 1px rgba(255,255,255,.35);
}

/* 살짝 뜨는 입장 애니메이션 */
@keyframes cc-in{
  from{ transform:translateY(6px); opacity:0; }
  to{   transform:translateY(0);   opacity:1; }
}
`;
    document.head.appendChild(st);
  }

  /* ===========================
   * 2) HTML
   * =========================== */
  function roomHTML() {
    const people = ['박소현','김형욱','양수빈','이민우','이재환'];
    return `
<section class="room-wrap">

  <!-- lobby -->
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

  <header class="room-topbar">
    <div class="room-title">저녁 뭐 먹을까? 치맥? 족발? 육회? 삼쏘? 곱창에 쏘주? 골라 골라</div>
  </header>

  <div class="room-body">
    <div class="room-stage">
      <div class="stage-icon" aria-hidden="true">
        <span class="ring"></span><span class="ring r2"></span>
        <svg class="voice-mic" viewBox="0 0 48 48" role="img" aria-label="음성 회의 마이크">
          <defs>
            <linearGradient id="micGrad" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0" stop-color="#7C3AED"/><stop offset="1" stop-color="#06B6D4"/>
            </linearGradient>
            <filter id="micGlow" x="-60%" y="-60%" width="220%" height="220%">
              <feGaussianBlur stdDeviation="2.2" result="b"/><feMerge>
                <feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge>
            </filter>
          </defs>
          <g stroke="url(#micGrad)" stroke-width="3" fill="none" stroke-linecap="round" stroke-linejoin="round" filter="url(#micGlow)">
            <rect x="17" y="8" width="14" height="22" rx="7"/>
            <path d="M12 22v1a12 12 0 0 0 24 0v-1"/>
            <path d="M24 36v6M18 42h12"/>
          </g>
        </svg>
      </div>

      <!-- Hover HUD -->
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
          <span class="rp-ic">👥</span><span>참가자</span>
          <span class="rp-count" id="rp-count">0</span>
        </div>
        <input class="rp-search" type="text" placeholder="검색" />
      </div>
      <ul class="room-people-list">
        ${people.map((n,i)=>`
          <li class="person">
            <div class="avatar av-${i%5}"><span>${n[0]}</span></div>
            <div class="p-main"><div class="p-name">${n}</div><div class="p-sub">온라인</div></div>
            <div class="p-actions"><button class="chip" title="음소거">🔇</button><button class="chip" title="내보내기">⛔</button></div>
          </li>
        `).join('')}
      </ul>
      <button class="btn-ghost" id="btn-more">더보기</button>
    </aside>
  </div>

  <!-- 회의록 모달 -->
  <div id="notes-modal" class="notes-modal" aria-hidden="true">
    <div class="notes-card" role="dialog" aria-modal="true" aria-labelledby="notes-title">
      <div class="notes-head">
        <strong id="notes-title">회의록</strong>
        <button id="btn-close-notes" class="btn btn-outline" type="button">닫기</button>
      </div>
      <div id="notes-body" class="notes-body">(아직 내용이 없습니다)</div>
      <div class="notes-actions">
        <button id="btn-clear-notes"  class="btn btn-outline" type="button">비우기</button>
        <button id="btn-export-notes" class="btn btn-outline" type="button">텍스트 저장</button>
      </div>
    </div>
  </div>
</section>`;
  }

  /* ===========================
   * 3) Events
   * =========================== */
  function bindRoomEvents(root){
    // HUD
    const startBtn    = root.querySelector('#hud-notes-start');
    const stopBtn     = root.querySelector('#hud-notes-stop');
    const openBtn     = root.querySelector('#hud-open-notes');
    const endBtn      = root.querySelector('#hud-end');
    const micSel      = root.querySelector('#hud-mic');

    // 회의록 모달
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
      const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = 'meeting-notes.txt'; a.click();
      URL.revokeObjectURL(a.href);
    });

    // 작성 시작/종료
    function beginNotes(){
      startBtn.disabled = true; stopBtn.disabled = false;
      openBtn.classList.remove('show'); // 작성 중에는 숨김
      Speech.start();
      root.classList.add('rec-on');     // 네온 링 on
    }
    function endNotes(){
      startBtn.disabled = false; stopBtn.disabled = true;
      Speech.stop();
      root.classList.remove('rec-on');
      // 회의록 버튼을 오른쪽으로 "밀리며" 등장
      requestAnimationFrame(()=> openBtn.classList.add('show'));
    }

    startBtn?.addEventListener('click', beginNotes);
    stopBtn ?.addEventListener('click', endNotes);

    // 통화 종료 → 토스트만
    endBtn?.addEventListener('click', () => {
      // 작성 중이면 정리
      if (!startBtn.disabled) {} else { endNotes(); }
      const toast = document.createElement('div');
      toast.className = 'end-toast';
      toast.textContent = '회의가 종료되었습니다.';
      document.body.appendChild(toast);
      requestAnimationFrame(()=>toast.classList.add('in'));
      setTimeout(()=>{ toast.classList.remove('in'); setTimeout(()=>toast.remove(),180); }, 2000);
    });

    // 로비 수락/거절
const lobby = root.querySelector('#lobby-view');
root.querySelector('#btn-join')?.addEventListener('click', () => {
  lobby?.remove();
  // 포커스는 네가 원하던 버튼으로 이동
  root.querySelector('#hud-notes-start')?.focus();
});

// ▼ 거절: 컨테이너 내용을 '취소 화면'으로 교체
root.querySelector('#btn-decline')?.addEventListener('click', () => {
  const mount = root.parentElement || document.body;
  mount.innerHTML = `
    <section class="cancel-state">
      <div class="cancel-card">참가 요청이 취소되었습니다.</div>
    </section>
  `;
});

    // 마이크 목록(가능하면 채우기)
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

  /* ===========================
   * 4) Speech (Web Speech API – 라이트)
   * =========================== */
  const SR = w.SpeechRecognition || w.webkitSpeechRecognition;
  const Speech = (function(){
    let recog=null, running=false;
    function ensure(){
      if (recog) return recog;
      if (!SR){ alert('이 브라우저는 음성 인식을 지원하지 않습니다.'); return null; }
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
      start(){
        const r = ensure(); if (!r || running) return;
        r.start(); running=true;
      },
      stop(){
        if (!recog || !running) return;
        try{ recog.stop(); }catch(e){}
        running=false;
      }
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
      bindRoomEvents(el.querySelector('.room-wrap'));
    }
  };
  w.Meeting = Meeting;
})(window);
