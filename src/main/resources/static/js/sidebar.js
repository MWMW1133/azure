// ===== sidebar.js =====
// 클릭 시 본문 전환 + 백엔드 연결 포인트 + 상태 팝오버
// + 폰트 크기/굵기(타이포) 런타임 오버레이 주입
window.__SPA_NAV_ENABLED__ = false;

// 중복 로드 가드
if (!window.__SIDEBAR_LOADED__) {
  window.__SIDEBAR_LOADED__ = true;

(function () {
  /* ──[A] 사이드바 타이포(크기/굵기) 런타임 주입 ────────────────────────────
     JSP 캐시/우선순위 문제를 피하려고, 동일 규칙을 head에 한 번 더 주입.
     아래 숫자만 바꾸면 전체가 바로 적용된다. (JSP의 <style id="sb-typo-vars">가 있으면 그대로 두고,
     이 주입은 '최후 보정' 역할이라 중복되어도 문제 없음) */
  const TYPO = {
    sbFs: 17, // 기본 크기(px)
    sbFw: 430, // 기본 굵기(얇게: 380~440)
    labelFs: 24,
    labelFw: 720,
    titleFw: 520,
    userFw: 560,
    presFw: 520,
  };
  (function injectTypography() {
    if (document.getElementById('sb-typo-runtime')) return;
    const css = `
      :root{
        --sb-fs:${TYPO.sbFs}px; --sb-fw:${TYPO.sbFw};
        --sb-label-fs:${TYPO.labelFs}px; --sb-label-fw:${TYPO.labelFw};
        --sb-nav-fw:var(--sb-fw); --sb-proj-fw:var(--sb-fw);
        --sb-title-fw:${TYPO.titleFw}; --sb-user-fw:${TYPO.userFw}; --sb-pres-fw:${TYPO.presFw};
      }
      .nav-fixed .nav-item > span:last-child{
        font-size:var(--sb-fs) !important; font-weight:var(--sb-nav-fw) !important; letter-spacing:-0.012em;
      }
      .group-title > span:last-child{
        font-size:var(--sb-fs) !important; font-weight:var(--sb-title-fw) !important; letter-spacing:-0.006em; color:#2b3447;
      }
      .proj-row > span:last-child{
        font-size:var(--sb-fs) !important; font-weight:var(--sb-proj-fw) !important; letter-spacing:-0.006em; color:#1e293b;
      }
      .label{
        font-size:var(--sb-label-fs) !important; font-weight:var(--sb-label-fw) !important; letter-spacing:-0.012em;
      }
      .user-name{
        font-size:var(--sb-fs) !important; font-weight:var(--sb-user-fw) !important; letter-spacing:-0.012em; color:#0f172a;
      }
      .presence{
        font-size:15px !important; font-weight:var(--sb-pres-fw) !important; letter-spacing:0; color:#6b7280;
      }`;

    const st = document.createElement('style');
    st.id = 'sb-typo-runtime';
    st.textContent = css;
    document.head.appendChild(st);
  })();
  /* ─────────────────────────────────────────────────────────────────── */

  //  const main = document.querySelector('.main');
  const main = document.querySelector('.page-body'); // 수정

  // 글씨 bold처리 문제 ========================================
  // active 토글 전용 함수
  function setActiveNav(target) {
    if (!target) return;
    document.querySelectorAll('.nav-item, .proj-row').forEach((el) => el.classList.remove('active'));
    target.classList.add('active');
  }

  // 상단 고정 3개
  (function bindFixed() {
    const fixed = document.querySelectorAll('.nav-fixed .nav-item');
    if (fixed[0])
      fixed[0].addEventListener('click', (e) => {
        try { Router.go('home'); } catch(e2) {}
        setActiveNav(e.currentTarget);
      });
    if (fixed[1])
      fixed[1].addEventListener('click', (e) => {
        try { Router.go('tasks'); } catch(e2) {}
        setActiveNav(e.currentTarget);
      });
    if (fixed[2])
      fixed[2].addEventListener('click', (e) => {
        try { Router.go('calendar'); } catch(e2) {}
        setActiveNav(e.currentTarget);
      });
  })();


  // 프로젝트 목록 버튼
  document.querySelectorAll('.proj-list .proj-row').forEach((btn, i) => {
    const name = btn.querySelector('span:last-child')?.textContent?.trim() || '프로젝트 ' + (i + 1);
    const id = 11 + i; // 더미 ID
    // btn.addEventListener('click', () => Router.go('project', { id, name }));
    btn.addEventListener('click', (e) => {
      try { Router.go('project', { id, name }); } catch(e2) {}
      setActiveNav(e.currentTarget);
    });
  });

  // 프로젝트 계획
  const planBtn = document.querySelector('.proj-row.proj-plan');
  // if (planBtn) planBtn.addEventListener('click', () => Router.go('plan'));
  if (planBtn)
    planBtn.addEventListener('click', (e) => {
      try { Router.go('plan'); } catch(e2) {}
      setActiveNav(e.currentTarget);
    });

  // 회의실
  const roomBtn = document.querySelector('.proj-row.room');
  // if (roomBtn) planBtn.addEventListener('click', () => Router.go('room'));
  if (roomBtn)
    roomBtn.addEventListener('click', (e) => {
      try { Router.go('room'); } catch(e2) {}
      setActiveNav(e.currentTarget);
    });

  // 초기 active: 홈 버튼
  // const firstNav = document.querySelector(".nav-fixed .nav-item");
  // if (firstNav) setActiveNav(firstNav);

  const currentActive = document.querySelector(".nav-fixed .nav-item.active");
  if (currentActive) {
    setActiveNav(currentActive); // 서버가 붙인 active 유지
  }


  // ===== Presence Status (상태 선택 팝오버) =====
  const PRESENCE = {
    online: { label: '접속중', color: '#22c55e' }, // green
    busy: { label: '다른 용무중', color: '#f59e0b' }, // amber
    away: { label: '자리 비움', color: '#ef4444' }, // red
    offline: { label: '오프라인', color: '#9ca3af' }, // gray
  };

  const presenceEl = document.querySelector('.presence');
  const presenceDot = presenceEl?.querySelector('.dot');

  // 팝오버 스타일(팝오버 전용) — 부족하면 자동 주입
  function ensurePresenceStyles() {
    if (document.getElementById('presence-style')) return;

    const css = `
      .status-popover{
        position:fixed; z-index:1000; background:#fff;
        border:1px solid rgba(0,0,0,.08); border-radius:12px;
        box-shadow:0 10px 24px rgba(0,0,0,.12);
        padding:6px; width:160px; display:none;
      }
      .status-item{
        display:flex; align-items:center; gap:10px;
        padding:8px 10px; border-radius:10px; cursor:pointer;
        font-size:14px;
      }
      .status-item:hover{ background:var(--hover); }
      .status-dot{ width:12px; height:12px; border-radius:999px; border:1px solid rgba(0,0,0,.06); }
      .status-item:focus{ outline:2px solid #93c5fd; outline-offset:2px; }
    `;

    const st = document.createElement('style');
    st.id = 'presence-style';
    st.textContent = css;
    document.head.appendChild(st);
  }

  // 팝오버 생성(메인 영역에 뜨게 body에 부착)
  let pop;
  function ensurePopover() {
    if (pop) return pop;
    ensurePresenceStyles();

    pop = document.createElement('div');
    pop.className = 'status-popover';
    pop.setAttribute('role', 'menu');
    pop.style.display = 'none';
    pop.innerHTML = [
      { key: 'online', ...PRESENCE.online },
      { key: 'busy', ...PRESENCE.busy },
      { key: 'away', ...PRESENCE.away },
      { key: 'offline', ...PRESENCE.offline },
    ]
      .map(
        (s) => `
      <div class="status-item" role="menuitem" tabindex="0" data-key="${s.key}">
        <span class="status-dot" style="background:${s.color}"></span>
        <span>${s.label}</span>
      </div>
    `
      )
      .join('');

    pop.addEventListener('click', (e) => {
      const item = e.target.closest('.status-item');
      if (!item) return;
      setPresence(item.dataset.key);
      hidePopover();
    });

    pop.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' || e.key === ' ') {
        const item = document.activeElement.closest('.status-item');
        if (item) {
          setPresence(item.dataset.key);
          hidePopover();
        }
      }
    });

    document.body.appendChild(pop);
    return pop;
  }

  // 오른쪽(사이드바 경계 기준) 위치
  function showPopover() {
    if (!presenceEl) return;
    const p = ensurePopover();
    p.style.display = 'block';

    const hostRect = presenceEl.getBoundingClientRect();
    const sideRect = document.querySelector('.sidebar')?.getBoundingClientRect?.() || { right: 0 };
    const pRect = p.getBoundingClientRect();
    const vw = window.innerWidth;
    const vh = window.innerHeight;

    const EDGE_GAP = 6; // 사이드바 경계 기준 기본 간격
    const NUDGE_X = -120; // 음수=왼쪽(경계쪽), 양수=오른쪽
    const Y_MARGIN = 8;

    let left = sideRect.right + EDGE_GAP + NUDGE_X;
    let top = hostRect.top + (hostRect.height - pRect.height) / 2;

    left = Math.max(0, Math.min(left, vw - pRect.width - 8));
    top = Math.max(Y_MARGIN, Math.min(top, vh - pRect.height - Y_MARGIN));

    p.style.left = `${left}px`;
    p.style.top = `${top}px`;

    setTimeout(() => document.addEventListener('mousedown', onDocDown));
  }

  function hidePopover() {
    if (pop) pop.style.display = 'none';
    document.removeEventListener('mousedown', onDocDown);
  }

  function onDocDown(e) {
    if (!pop || !presenceEl) return;
    if (pop.contains(e.target) || presenceEl.contains(e.target)) return;
    hidePopover();
  }

  function setPresence(key) {
    const conf = PRESENCE[key] || PRESENCE.online;
    if (presenceDot) presenceDot.style.background = conf.color;
    if (presenceEl) {
      presenceEl.setAttribute('data-status', key);
      presenceEl.childNodes.forEach((n) => {
        if (n.nodeType === 3) n.remove();
      });
      presenceEl.appendChild(document.createTextNode(' ' + conf.label));
    }
    try {
      localStorage.setItem('presence', key);
    } catch (e) {}
    /* [백엔드 연결 지점]
       fetch('/api/me/status', {
         method:'POST', headers:{'Content-Type':'application/json'},
         body: JSON.stringify({ status: key })
       });
    */
  }

  // 초기 상태 복원 + 토글 바인딩
  (function initPresence() {
    if (!presenceEl) return;
    const saved = (() => {
      try {
        return localStorage.getItem('presence');
      } catch (e) {
        return null;
      }
    })();
    if (saved && PRESENCE[saved]) setPresence(saved);
    else setPresence('online');

    presenceEl.style.cursor = 'pointer';
    presenceEl.setAttribute('title', '상태 변경');
    presenceEl.setAttribute('tabindex', '0');

    presenceEl.addEventListener('click', () => {
      if (pop && pop.style.display === 'block') hidePopover();
      else showPopover();
    });
    presenceEl.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        if (pop && pop.style.display === 'block') hidePopover();
        else showPopover();
      }
    });
  })();

  // // === Meeting SPA mount ===
  // (function bindMeetingNav() {
  //   function mountMeeting() {
  //     const mountTarget = document.querySelector('.page-body');
  //     if (!mountTarget) return;
  //
  //     // 본문에 회의실 UI 렌더
  //     if (window.Meeting && typeof window.Meeting.mount === 'function') {
  //       window.Meeting.mount(mountTarget);
  //     } else {
  //       // meeting.js가 아직 안 들어왔을 때 대비(거의 필요 없지만 안전핀)
  //       const s = document.createElement('script');
  //       s.src = (window.APP_CTX || '') + '/js/meeting.js?v=spa_mount';
  //       s.onload = () => window.Meeting?.mount(mountTarget);
  //       document.body.appendChild(s);
  //     }
  //
  //     // 사이드바 active 표시
  //     document.querySelectorAll('.nav-item, .proj-row').forEach((el) => el.classList.remove('active'));
  //     const link = document.querySelector('.proj-row.room');
  //     if (link) link.classList.add('active');
  //   }
  //
  //   // 사이드바 전체에 이벤트 위임 (캡처 단계에서 가장 먼저 가로채기)
  //   const sidebar = document.querySelector('.sidebar');
  //   if (sidebar) {
  //     sidebar.addEventListener(
  //       'click',
  //       function (e) {
  //         const a = e.target.closest('.proj-row.room');
  //         if (!a) return;
  //         e.preventDefault();
  //         e.stopPropagation();
  //         mountMeeting();
  //       },
  //       true
  //     );
  //   }
  //
  //   // 혹시 위임이 적용되기 전 클릭을 잡아주기 위한 2중 안전핀
  //   const roomLink = document.querySelector('.proj-row.room');
  //   if (roomLink) {
  //     roomLink.addEventListener(
  //       'click',
  //       function (e) {
  //         e.preventDefault();
  //         e.stopPropagation();
  //         mountMeeting();
  //       },
  //       true
  //     );
  //   }
  // })();


// === Meeting SPA mount ===
(function bindMeetingNavOnce(){
  // ★ 중복 바인딩 방지
  if (window.__bindMeetingNav) return;
  window.__bindMeetingNav = true;

  // === SAFEGUARD: stray meeting link fix ===
[...document.querySelectorAll('a.proj-row.room')].forEach(n=>{
  const target = document.querySelector('.sidebar .proj-list');
  if (target && !n.closest('.sidebar')) target.appendChild(n);
});

  const MAINBAR = (window.APP_CTX || '') + '/mainbar';

  function setActiveForMeeting(){
    document.querySelectorAll('.nav-item, .proj-row').forEach(el => el.classList.remove('active'));
    const link = document.querySelector('.proj-row.room') || document.querySelector('#nav-room');
    if (link) link.classList.add('active');
  }

  // meeting.js 동적 주입을 한 번만
  function ensureMeetingScriptLoaded(cb){
    if (window.Meeting && typeof window.Meeting.mount === 'function') {
      cb(); return;
    }
    let tag = document.getElementById('meeting-js');
    if (!tag) {
      tag = document.createElement('script');
      tag.id = 'meeting-js';                         // ★ 중복 주입 방지
      tag.src = (window.APP_CTX || '') + '/js/meeting.js?v=spa_mount';
      tag.onload = cb;
      document.body.appendChild(tag);
    } else {
      const done = () => cb();
      if (tag.readyState) tag.onreadystatechange = done;
      else tag.addEventListener('load', done, { once: true });
    }
  }

  function mountMeeting(withReset = true) {
    const mountTarget = document.querySelector('.page-body') || document.body;
    if (!mountTarget) return;

    if (window.resetStubStore) window.resetStubStore();

    ensureMeetingScriptLoaded(() => {
      // ★ meeting.js 내부에도 __meetingMounted 가드가 있으면 더 안전
      window.Meeting?.mount(mountTarget);

      // 첫 화면 리셋
      if (withReset && typeof window.resetMeetingUI === 'function') {
        requestAnimationFrame(() => window.resetMeetingUI(mountTarget));
      }

      // 사이드바 active 처리
      setActiveForMeeting();

      // 입장 모달(중복 방지는 모달 함수 내부에서 처리 가정)
      const root = document.querySelector('.room-wrap') || document;
      if (window.confirmJoinMeeting) {
        window.confirmJoinMeeting(root);
      }
    });
  }

    // 전역 스위치(없으면 기본 false = 가로채기 안 함)
    if (typeof window.__SPA_NAV_ENABLED__ === 'undefined') {
      window.__SPA_NAV_ENABLED__ = false;
    }

    // ▼ 클릭 위임: 회의실로 진입 (한 번만 바인딩됨)
    document.addEventListener('click', (e) => {
      const a = e.target.closest('a[data-route="meeting"], .proj-row.room, #nav-room');
      if (!a) return;

      // 1) SPA 가로채기 전역 OFF면 그대로 브라우저 네비게이션
      if (window.__SPA_NAV_ENABLED__ === false) return;

      // 2) 새 탭/중클릭 등은 건드리지 않음
      if (e.metaKey || e.ctrlKey || e.shiftKey || e.button === 1) return;

      // 3) 삽입 대상 없으면 가로채지 말고 서버 라우팅
      const container = document.querySelector('.page-body');
      if (!container) return;

      // === 여기서만 SPA 전환 ===
      e.preventDefault();
      history.pushState({}, '', MAINBAR);
      mountMeeting(true);
    });

    // (선택) URL이 /meeting 으로 들어온 경우 SPA로 전환
    // if (window.__SPA_NAV_ENABLED__ && location.pathname === (window.APP_CTX || '') + '/meeting') {
    //   const container = document.querySelector('.page-body');
    //   if (container) {
    //     history.replaceState({}, '', MAINBAR);
    //     mountMeeting(true);
    //   }
    // }
})();

  // DM 리스트 동적 로드 (팀원 목록)
  // ========== DM 리스트 (팀원) 렌더: 모달 안에서만 ==========
// ※ 이 블록은 모달 내부에 #dm-list 컨테이너가 있을 때만 렌더링합니다.
//   홈/사이드바 레이아웃에는 아무것도 추가하지 않으므로 레이아웃을 밀지 않습니다.
  


  /* ========= [백엔드 연결 예시 – 이 주석만 보고 교체] =========
  // 1) 유저 정보 로드
  async function loadMe(){
    const r = await fetch('/api/me');
    const me = await r.json();
    document.querySelector('.user-card .user-name').textContent = me.name;
    document.querySelector('.user-card .avatar').textContent = me.name.charAt(0);
  }
  // loadMe();

  // 2) 프로젝트 목록 로드
  async function loadProjects(){
    const r = await fetch('/api/projects');
    const items = await r.json(); // [{id,name}, ...]
    const list = document.querySelector('.proj-list');
    list.innerHTML = '';
    for (const p of items){
      const btn = document.createElement('button');
      btn.className = 'proj-row';
      btn.innerHTML = `
        <span class="ic elbow">
          <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
            <path d="M6 6v8a4 4 0 0 0 4 4h8"></path>
          </svg>
        </span>
        <span></span>`;
      btn.querySelector('span:last-child').textContent = p.name;
      btn.addEventListener('click', () => Router.go('project', { id:p.id, name:p.name })); 
      list.appendChild(btn);
    }
  }
  // loadProjects();
  ===================================================== */
})();
} // __SIDEBAR_LOADED__ guard end
