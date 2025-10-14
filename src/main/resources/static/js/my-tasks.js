(function () {
  const root = document.getElementById('task-list-root');
  if (!root || window.__TASK_LIST_MOUNTED__) return;
  window.__TASK_LIST_MOUNTED__ = true;

  const USERS_API = root.dataset.usersApi || null;

  // ---------- 유틸 ----------
  const esc = (s) => (s == null ? '' : String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;'));

  function clamp01to100(n) {
    const x = Number(n ?? 0);
    if (Number.isNaN(x)) return 0;
    return Math.max(0, Math.min(100, x));
  }

  function fmtShortDate(input) {
    if (input == null || input === '') return '-';

    // Date 객체면 바로 포맷
    if (Object.prototype.toString.call(input) === '[object Date]' && !isNaN(input)) {
      const yy = String(input.getFullYear()).slice(-2);
      const mm = String(input.getMonth() + 1).padStart(2, '0');
      const dd = String(input.getDate()).padStart(2, '0');
      return `${yy}-${mm}-${dd}`;
    }

    let s = String(input).trim();
    if (!s) return '-';

    // yyyy-mm-dd 빠른 경로
    const m1 = s.match(/^(\d{4})-(\d{2})-(\d{2})$/);
    if (m1) return `${m1[1].slice(-2)}-${m1[2]}-${m1[3]}`;

    // 타임스탬프(초/밀리초)
    if (/^\d+$/.test(s)) {
      const n = Number(s);
      const ms = n > 1e12 ? n : n * 1000;
      const d = new Date(ms);
      if (!isNaN(d)) {
        const yy = String(d.getFullYear()).slice(-2);
        const mm = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        return `${yy}-${mm}-${dd}`;
      }
    }

    // 일반 문자열: 공백 → T 로 치환 후 파싱
    if (s.includes(' ')) s = s.replace(' ', 'T');
    const d = new Date(s);
    if (isNaN(d)) return '-';

    const yy = String(d.getFullYear()).slice(-2);
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yy}-${mm}-${dd}`;
  }

  function normalizeDates(scope) {
    (scope || document).querySelectorAll('.task-row').forEach((row) => {
      const startEl = row.querySelector('[data-cell="startDate"]');
      const dueEl = row.querySelector('[data-cell="dueDate"]');
      const updatedTextEl = row.querySelector('[data-cell="updatedAt"] .updated-at-text');

      if (startEl) {
        const raw = startEl.getAttribute('data-raw') || startEl.textContent.trim();
        startEl.textContent = fmtShortDate(raw);
      }
      if (dueEl) {
        const raw = dueEl.getAttribute('data-raw') || dueEl.textContent.trim();
        dueEl.textContent = fmtShortDate(raw);
      }
      if (updatedTextEl) {
        const raw = updatedTextEl.getAttribute('data-raw') || updatedTextEl.textContent.trim();
        updatedTextEl.textContent = fmtShortDate(raw);
      }
    });
  }

  // ---------- 렌더: 진행률 ----------
  function applyAllProgressBars(scope) {
    (scope || document).querySelectorAll('.task-progress-bar').forEach((bar) => {
      const pct = clamp01to100(bar.dataset.progress);
      bar.style.width = pct + '%';
      const val = bar.closest('.progress-cell')?.querySelector('.progress-value');
      if (val) val.textContent = `${pct}%`;
    });
  }

  // ---------- 렌더: 우선순위 ----------
  const PRIORITY_MAP = {
    // id 기준(1~5) / name 기준(대문자 영문)
    byId: {
      5: { label: '매우 높음', color: '#ef4444', name: 'HIGHEST' },
      4: { label: '높음', color: '#f59e0b', name: 'HIGH' },
      3: { label: '보통', color: '#22c55e', name: 'MEDIUM' },
      2: { label: '낮음', color: '#3b82f6', name: 'LOW' },
      1: { label: '매우 낮음', color: '#64748b', name: 'LOWEST' },
    },
    alias(s) {
      if (!s) return '';
      const up = String(s).trim().toUpperCase();
      const dict = {
        HIGHEST: 'HIGHEST',
        'VERY HIGH': 'HIGHEST',
        매우높음: 'HIGHEST',
        HIGH: 'HIGH',
        높음: 'HIGH',
        MEDIUM: 'MEDIUM',
        NORMAL: 'MEDIUM',
        보통: 'MEDIUM',
        LOW: 'LOW',
        낮음: 'LOW',
        LOWEST: 'LOWEST',
        'VERY LOW': 'LOWEST',
        매우낮음: 'LOWEST',
      };
      return dict[up] || '';
    },
  };

  function applyPriorityBadges(scope) {
    (scope || document).querySelectorAll('.task-row').forEach((row) => {
      const badge = row.querySelector('.priority-cell .priority-badge');
      if (!badge) return;

      let idStr = (row.dataset.priorityId || badge.dataset.id || '').toString().trim();
      let nameStr = (row.dataset.priorityName || badge.dataset.name || '').toString().trim();

      let meta = null;
      if (idStr) {
        const n = parseInt(idStr, 10);
        if (!Number.isNaN(n)) meta = PRIORITY_MAP.byId[Math.max(1, Math.min(5, n))] || null;
      }
      if (!meta && nameStr) {
        const norm = PRIORITY_MAP.alias(nameStr);
        meta = Object.values(PRIORITY_MAP.byId).find((m) => m.name === norm) || null;
      }

      const dot = badge.querySelector('.priority-dot');
      const text = badge.querySelector('.priority-text');
      if (meta) {
        if (dot) {
          dot.style.setProperty('background', meta.color, 'important');
          dot.style.setProperty('background-color', meta.color, 'important');
        }
        if (text) text.textContent = meta.label;
      } else {
        if (dot) {
          dot.style.setProperty('background', '#e5e7eb', 'important');
          dot.style.setProperty('background-color', '#e5e7eb', 'important');
        }
        if (text) text.textContent = '-';
      }
    });
  }

  // ---------- 렌더: 담당자 ----------
  // users API 응답 형식을 다음 중 하나로 가정:
  // 1) [{id, name, avatarUrl}, ...]
  // 2) { content: [{id, name, avatarUrl}, ...] } (페이지 응답)
  async function fetchUsersMapOnce() {
    if (!USERS_API) return null;
    try {
      const res = await fetch(USERS_API);
      if (!res.ok) throw new Error('users api failed');
      const json = await res.json();
      const arr = Array.isArray(json) ? json : Array.isArray(json?.content) ? json.content : [];
      const map = new Map();
      arr.forEach((u) => {
        if (u && u.id != null) {
          map.set(String(u.id), {
            id: String(u.id),
            name: u.name || '',
            avatarUrl: u.avatarUrl || null,
          });
        }
      });
      return map;
    } catch (e) {
      console.warn('사용자 목록을 불러오지 못했습니다.', e);
      return null;
    }
  }

  function renderAssigneeCell(row, usersMap) {
    const cell = row.querySelector('.assignee-cell');
    if (!cell) return;

    const id = (row.dataset.assigneeId || '').toString().trim();
    const localName = (row.dataset.assigneeName || '').toString().trim();
    const localAvatar = (row.dataset.assigneeAvatar || '').toString().trim();

    // 1) row에 싣린 값이 있으면 그걸 최우선 사용
    if (localAvatar) {
      cell.innerHTML = `<img src="${esc(localAvatar)}" class="assignee-img" alt="${esc(localName || '')}" />`;
      return;
    }
    if (localName) {
      cell.innerHTML = `<div class="assignee-initial">${esc(localName[0] || 'U')}</div>`;
      return;
    }

    // 2) 로컬에 없고 id만 있으면, usersMap(있으면)에서 보조
    if (id && usersMap) {
      const u = usersMap.get(id);
      if (u && u.avatarUrl) {
        cell.innerHTML = `<img src="${esc(u.avatarUrl)}" class="assignee-img" alt="${esc(u.name || '')}" />`;
        return;
      }
      if (u && u.name) {
        cell.innerHTML = `<div class="assignee-initial">${esc(u.name[0] || 'U')}</div>`;
        return;
      }
    }

    // 3) 그래도 없으면 플레이스홀더
    cell.innerHTML = `<div class="assignee-placeholder">-</div>`;
  }

  async function applyAllAssignees() {
    const rows = [...document.querySelectorAll('.task-row')];
    const needFetch = rows.some((r) => r.dataset.assigneeId && !r.dataset.assigneeName && !r.dataset.assigneeAvatar && root.dataset.usersApi);

    const usersMap = needFetch ? await fetchUsersMapOnce() : null;
    rows.forEach((row) => renderAssigneeCell(row, usersMap));
  }

  // ---------- 날짜 텍스트 보정(선택) ----------
  function normalizeDates(scope) {
    (scope || document).querySelectorAll('.task-row').forEach((row) => {
      const startEl = row.querySelector('[data-cell="startDate"]');
      const dueEl = row.querySelector('[data-cell="dueDate"]');
      const updatedEl = row.querySelector('[data-cell="updatedAt"] .updated-at-text');
      if (startEl) startEl.textContent = fmtShortDate(startEl.textContent.trim());
      if (dueEl) dueEl.textContent = fmtShortDate(dueEl.textContent.trim());
      if (updatedEl) updatedEl.textContent = fmtShortDate(updatedEl.textContent.trim());
    });
  }

  // ---------- 부트 ----------
  (async function init() {
    applyAllProgressBars(root);
    applyPriorityBadges(root);
    normalizeDates(root);
    await applyAllAssignees();

    // DOM 변경(행 추가/삭제) 시 진행률/우선순위/담당자 다시 적용
    const mo = new MutationObserver(async (mut) => {
      let needReapply = false;
      for (const m of mut) {
        if (m.addedNodes?.length || m.removedNodes?.length) {
          needReapply = true;
          break;
        }
      }
      if (!needReapply) return;
      applyAllProgressBars(root);
      applyPriorityBadges(root);
      normalizeDates(root);
      await applyAllAssignees();
    });
    mo.observe(root, { childList: true, subtree: true });
  })();
})();
