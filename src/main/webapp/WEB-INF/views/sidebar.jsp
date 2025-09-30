<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<html lang="ko">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Sidebar – Unified Buttons</title>

  <!-- 원본 폰트 링크 (변경 금지) -->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/variable/pretendardvariable-dynamic-subset.css">

  <!-- ▼ 기존 sidebar.css 내용을 그대로 내장 -->
  <style id="sb-typo-vars">
  /* === Team Sidebar spec (300px x 100dvh) === */
  :root{
    --sidebar-w:300px;
    --page-bg:#f6f8fb;
    --text:#111827;
    --muted:#5b6b83;
    --border:#e6ebf3;
    --hover:#eef3ff;

    /* gradient (B5CEFB 24→20→16%) */
    --g0:rgba(181,206,251,.24);
    --g1:rgba(181,206,251,.20);
    --g2:rgba(181,206,251,.16);

    --shadow-card:0 12px 24px rgba(0,0,0,.10);
    --shadow-pill:0 8px 14px rgba(0,0,0,.15);

    /* UI 폰트 고정 */
    --font-ui:"Pretendard Variable","Noto Sans KR",
               Inter,"Apple SD Gothic Neo",system-ui,-apple-system,
               "Segoe UI",Roboto,Helvetica,Arial,sans-serif;
  }

  *{ box-sizing:border-box; }
  html,body{ height:100%; }
  body{
    margin:0; background:var(--page-bg); color:var(--text);
    font-family: Inter,"Noto Sans KR","Apple SD Gothic Neo","Segoe UI",
                 Roboto,Helvetica,Arial,system-ui,-apple-system,sans-serif;
    line-height:1.5;
    -webkit-font-smoothing:antialiased; -moz-osx-font-smoothing:grayscale;
  }

  /* 레이아웃 */
  .app{
    min-height:100dvh;
    display:grid;
    grid-template-columns:var(--sidebar-w) 1fr;
  }

  /* 사이드바 */
  .sidebar{
    height:100dvh; padding:10px; position:relative;
    background:linear-gradient(180deg,var(--g0) 0%,var(--g1) 55%,var(--g2) 100%);
    border-right:1px solid rgba(0,0,0,.06);
  }
  .sidebar::before{
    content:""; position:absolute; inset:8px 8px 14px 8px; border-radius:20px;
    box-shadow:0 10px 20px rgba(0,0,0,.08); pointer-events:none;
  }

  /* 내용 스크롤 영역 */
  .sidebar-inner{
    height:calc(100dvh - 118px); /* 하단 카드 자리 확보 */
    overflow:auto; border-radius:20px; padding:18px 16px 8px;
  }

  /* 상단 고정 메뉴(텍스트 크기/굵기는 맨 아래 변수로 통일 제어) */
  .nav-fixed{ display:flex; flex-direction:column; gap:10px; margin:6px 0 45px; }
  .nav-item{
    display:flex; align-items:center; gap:12px;
    padding:12px 14px; border-radius:999px; border:1px solid transparent;
    background:transparent; cursor:pointer; text-align:left;
  }
  .nav-item:hover{ background:var(--hover); }
  .nav-item.active{ background:#eaf1ff; border-color:#eaf1ff; box-shadow:var(--shadow-pill); }

  /* 아이콘 공통 */
  .ic{ width:22px; height:22px; display:inline-grid; place-items:center; }
  svg{ display:block; }
  .stroke-1{ stroke:#111827; stroke-width:1.8; fill:none; stroke-linecap:round; stroke-linejoin:round; }

  /* 라벨/섹션 제목(폰트는 아래에서 변수로 덮어씀) */
  .label{ margin:0 6px 40px; color:#2f3747; }
  .group-title{
    display:flex; align-items:center; gap:10px;
    margin:0 6px 30px; color:#374151;
  }

  /* 프로젝트/일반 항목 (회의실 포함) */
  .proj-list{ display:flex; flex-direction:column; gap:10px; padding-left:2px; }
  .proj-row{
    display:flex; align-items:center; gap:12px;
    padding:12px 14px; border-radius:12px;
    background:transparent; border:0; cursor:pointer; text-align:left;
    color:#273449;
    -webkit-appearance:none; appearance:none;
  }
  .proj-row:hover{ background:var(--hover); }
  .elbow{ width:22px; color:#273449; }

  /* 프로젝트 계획은 살짝 아래 */
  .proj-plan{ margin-top:50px; }

  /* 구분선 */
  .divider{ height:1px; background:var(--border); border:0; margin:60px 6px; }

  /* 하단 사용자 카드 */
  .sidebar-footer{ position:absolute; left:20px; right:20px; bottom:16px; }

  .user-card{
    background:transparent; border:0; box-shadow:none;
    border-radius:20px; padding:14px;
    display:grid; grid-template-columns:44px 1fr; gap:12px;
  }
  .user-card:focus, .user-card:focus-visible{ outline:none; }

  .avatar{
    width:44px; height:44px; border-radius:999px; background:#95a4be;
    display:flex; align-items:center; justify-content:center; color:#fff; font-weight:800;
  }
  .user-name{ color:#1f2937; }
  .presence{ display:flex; align-items:center; gap:8px; color:#667085; }
  .dot{ width:10px; height:10px; border-radius:999px; background:#22c55e; }

  /* 본문(확인용) */
  .main{ padding:30px; }

  /* 회의실 전용 구분선 */
  .divider-room{ height:2px; background:#cfd7e6; margin:12px 6px; border-radius:2px; }
  .room-section{ margin-top:96px; }

  /* ===== FONT LOCK: 사이드바 폰트 고정 (기능 유지, 중복 제거) ===== */
  body, .sidebar, .sidebar *{
    font-family: var(--font-ui) !important;
    font-optical-sizing:auto;
    font-synthesis-weight:none;
    text-rendering:optimizeLegibility;
    -webkit-font-smoothing:antialiased;
    -moz-osx-font-smoothing:grayscale;
  }

  /* --- Presence 상태 팝오버 (뷰포트 기준) --- */
  .status-popover{
    position:fixed; z-index:1000; background:#fff;
    border:1px solid rgba(0,0,0,.08); border-radius:12px;
    box-shadow:0 10px 24px rgba(0,0,0,.12);
    padding:6px; width:160px; display:none;
  }
  .status-item{
    display:flex; align-items:center; gap:10px;
    padding:8px 10px; border-radius:10px; cursor:pointer;
  }
  .status-item:hover{ background:var(--hover); }
  .status-dot{ width:12px; height:12px; border-radius:999px; border:1px solid rgba(0,0,0,.06); }
  .status-item:focus{ outline:2px solid #93c5fd; outline-offset:2px; }

  /* ==============================================================
     ▼▼▼ 타이포 일괄 제어(최종 오버레이) — 이 블록 '하나'로만 관리 ▼▼▼
     ============================================================== */

  :root{
    /* 기본값 — 필요할 때 숫자만 조절 */
    --sb-fs: 17px;   /* 사이드바 공통 텍스트 크기 */
    --sb-fw: 430;    /* 사이드바 공통 굵기(얇게: 380~440 추천) */

    --sb-label-fs: 24px;  /* "워크 스페이스" 라벨 크기 */
    --sb-label-fw: 720;   /* 라벨 굵기 */

    /* 개별 미세 조정 */
    --sb-nav-fw:   var(--sb-fw); /* 홈/내 작업/내 캘린더 */
    --sb-proj-fw:  var(--sb-fw); /* 프로젝트 1~3 / 계획 / 회의실 */
    --sb-title-fw: 520;          /* "프로젝트" 제목줄 */
    --sb-user-fw:  560;          /* 하단 유저명 */
    --sb-pres-fw:  520;          /* "접속중" 텍스트 */
  }

   /* 텍스트만 정확히 덮어쓰기(아이콘/배경 영향 X). !important로 최종 승리 */
  .nav-fixed .nav-item > span:last-child{
    font-size:var(--sb-fs) !important; font-weight:var(--sb-nav-fw) !important; letter-spacing:-0.012em;
  }

  /*================= 수정시작 */
  /* nav-item과 proj-row를 모두 같은 active 스타일로 */
  .nav-item.active,
  .proj-row.active {
    background: #eaf1ff;
    border-color: #eaf1ff;
    box-shadow: var(--shadow-pill);
    border-radius: 999px;
  }

  /* active 상태 전용: nav-item, proj-row 모두 적용 */
  .nav-item.active > span:last-child,
  .proj-row.active > span:last-child {
    font-weight: 700 !important; /* bold */
    color: #000; /* 필요하면 색상 변경 */
  }

  /*================= 수정끝 */

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
  }

</style>
  <!-- ▲ CSS 끝 -->
</head>
<body>
  <div class="app">
    <aside class="sidebar" role="navigation" aria-label="Sidebar">
      <div class="sidebar-inner">
        <!-- 고정 3개 -->
        <nav class="nav-fixed">
          <button class="nav-item">
            <span class="ic">
              <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
                <path d="M3 10.5L12 4l9 6.5"></path>
                <path d="M6 10v8.5a1.5 1.5 0 0 0 1.5 1.5H16.5A1.5 1.5 0 0 0 18 18.5V10"></path>
              </svg>
            </span>
            <span>홈</span>
          </button>
          <button class="nav-item">
            <span class="ic">
              <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
                <path d="M3 7h18"></path>
                <rect x="4" y="7" width="16" height="12" rx="2"></rect>
                <path d="M9 3h6a2 2 0 0 1 2 2v2H7V5a2 2 0 0 1 2-2z"></path>
                <path d="M9 12h6"></path>
              </svg>
            </span>
            <span>내 작업</span>
          </button>
          <button class="nav-item">
            <span class="ic">
              <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
                <rect x="3" y="5" width="18" height="16" rx="3"></rect>
                <path d="M8 3v4M16 3v4M3 11h18"></path>
                <circle cx="16" cy="16" r="1.4"></circle>
              </svg>
            </span>
            <span>내 캘린더</span>
          </button>
        </nav>

        <div class="label">워크 스페이스</div>

        <div class="group">
          <div class="group-title">
            <span class="ic">
              <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
                <path d="M3 7h6l2 2h10v8a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3V7z"></path>
              </svg>
            </span>
            <span>프로젝트</span>
          </div>

          <!-- ▼▼▼ [백엔드 연결 지점] /api/projects 호출 후 아래 목록을 교체 렌더하세요 -->
          <div class="proj-list">
            <button class="proj-row">
              <span class="ic elbow">
                <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
                  <path d="M6 6v8a4 4 0 0 0 4 4h8"></path>
                </svg>
              </span>
              <span>프로젝트 1</span>
            </button>
            <button class="proj-row">
              <span class="ic elbow">
                <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
                  <path d="M6 6v8a4 4 0 0 0 4 4h8"></path>
                </svg>
              </span>
              <span>프로젝트 2</span>
            </button>
            <button class="proj-row">
              <span class="ic elbow">
                <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
                  <path d="M6 6v8a4 4 0 0 0 4 4h8"></path>
                </svg>
              </span>
              <span>프로젝트 3</span>
            </button>
          </div>
          <!-- ▲▲▲ [백엔드 연결 지점 끝] -->

          <button class="proj-row proj-plan">
            <span class="ic">
              <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
                <path d="M12 20h9"></path>
                <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L8 18l-4 1 1-4 11.5-11.5z"></path>
              </svg>
            </span>
            <span>프로젝트 계획</span>
          </button>
        </div>

        <div class="room-section">
          <hr class="divider divider-room" />
          <button class="proj-row room">
            <span class="ic">
              <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
                <path d="M4 13a8 8 0 0 1 16 0"></path>
                <path d="M4 13v5a2 2 0 0 0 2 2h2v-7H6a2 2 0 0 0-2 2z"></path>
                <path d="M20 13v5a2 2 0 0 1-2 2h-2v-7h2a2 2 0 0 1 2 2z"></path>
              </svg>
            </span>
            <span>회의실</span>
          </button>
          <hr class="divider divider-room" />
        </div>

        <div class="sidebar-footer">
          <div class="user-card">
            <!-- ▼▼▼ [백엔드 연결 지점] GET /api/me → {name} 받아서 아래 두 텍스트만 교체 -->
            <div class="avatar">이</div>
            <div>
              <div class="user-name">이재환</div>
              <div class="presence"><span class="dot"></span> 접속중</div>
            </div>
            <!-- ▲▲▲ -->
          </div>
        </div>
      </div>
    </aside>
  </div>

  <!-- 정적 JS 로드 -->
  <script src="/js/sidebar.js"></script>
</body>
</html>
