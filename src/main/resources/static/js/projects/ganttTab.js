// /static/js/gantt-tab.js
(function (global) {
  console.log('[GANTT JS] loaded');

  async function initGantt(ctx, projectId, opts = {}) {
    const sel = opts.container || '#gantt-wrap';
    const wrap = typeof sel === 'string' ? document.querySelector(sel) : sel;

    console.log('[initGantt] start', { ctx, projectId, container: sel, found: !!wrap });
    if (!wrap) return;

    // 중복 마운트 방지
    if (wrap.dataset.ganttMounted === '1') {
      console.log('[initGantt] already mounted, skip');
      return;
    }

    // 레이아웃 준비
    const ok = await ensureContainerLaidOut(wrap);
    if (!ok) {
      console.warn('[initGantt] container width still 0 after retries – abort');
      return;
    }

    fitGanttHeight(wrap, 32);

    wrap.dataset.ganttMounted = '1';

    //데이터
    let taskRows, workflows;
    try {
      [taskRows, workflows] = await Promise.all([
        // 부모 목록
        fetchJson(`${ctx}/api/projects/${projectId}/tasks`),
        // 워크플로우 팔레트
        fetchJson(`${ctx}/api/projects/${projectId}/workflows`),
      ]);
    } catch (err) {
      /* ... 생략 ... */
    }

    // 부모+자식 모두 평탄화해서 가져오기
    const rows = await loadTasksDeep(ctx, projectId, taskRows);

    // 색 스타일 주입
    injectWorkflowStyles(workflows);

    // 매핑
    const tasks = rows.map(toGanttTask).filter((t) => isYmd(t.start) && isYmd(t.end));

    if (!tasks.length) {
      renderMessage(wrap, '표시할 태스크가 없습니다.');
      return;
    }

    // 라이브러리 로드 확인
    if (typeof global.Gantt === 'undefined') {
      console.error('[initGantt] Frappe Gantt not loaded');
      renderMessage(wrap, '간트 라이브러리가 로드되지 않았습니다.');
      return;
    }

    // ===== 렌더 =====
    const gantt = new Gantt('#gantt', tasks, {
      view_mode: 'Week', // 'Day' | 'Week' | 'Month'
      bar_height: 28, // 막대 두께 살짝 키움
      padding: 40, // 행 간격 넉넉하게
      custom_popup_html: (task) => `
        <div class="details-container" style="min-width:220px">
          <h5 style="margin:0 0 6px 0">${escapeHtml(task.name)}</h5>
          <p style="margin:0 0 4px 0">${fmt(task._start)} ~ ${fmt(task._end)}</p>
          <p style="margin:0">진척: ${Math.round(task.progress)}%</p>
        </div>
      `,
      on_date_change: (task, start, end) =>
        patchJson(`${ctx}/api/tasks/${task.id}`, {
          startDate: ymd(start),
          dueDate: ymd(end),
        }).catch(console.error),
      on_progress_change: (task, progress) =>
        patchJson(`${ctx}/api/tasks/${task.id}/progress`, {
          progressPct: clamp01(progress),
        }).catch(console.error),
    });

    // 렌더 직후에도 한번 더 맞춤
    fitGanttHeight(wrap, 32);

    // 창/레이아웃 변화에 반응하여 높이 재계산
    const onResize = () => fitGanttHeight(wrap, 32);
    window.addEventListener('resize', onResize, { passive: true });

    const ro = new ResizeObserver(onResize);
    ro.observe(document.body);

    // 필요 시 해제함수 반환
    return () => {
      window.removeEventListener('resize', onResize);
      ro.disconnect();
      delete wrap.dataset.ganttMounted;
    };
  }

  // 높이 자동 맞춤
  function fitGanttHeight(wrap, bottomGap = 24) {
    // 뷰포트 높이
    const vp = document.documentElement.clientHeight;
    // wrap의 화면상 top
    const top = wrap.getBoundingClientRect().top;
    // 최소/최대(원하면 조절)
    const min = 360;
    const max = Math.round(vp * 0.92);
    // 남는 공간 = 화면 - top - 하단여백
    const h = Math.max(min, Math.min(max, vp - top - bottomGap));
    wrap.style.height = `${h}px`;
    wrap.style.overflow = 'auto';
    wrap.style.resize = 'vertical';
  }

  // 컨테이너 레이아웃 보장
  async function ensureContainerLaidOut(el, maxTries = 5) {
    for (let i = 0; i < maxTries; i++) {
      const w = el.getBoundingClientRect().width;
      if (w && w >= 10) return true;
      await nextFrame();
    }
    return false;
  }
  function nextFrame() {
    return new Promise((res) => requestAnimationFrame(() => requestAnimationFrame(res)));
  }

  // fetch/patch 유틸
  async function fetchJson(url) {
    const r = await fetch(url, { cache: 'no-cache' });
    if (!r.ok) throw new Error(`HTTP ${r.status} @ ${url}`);
    return r.json();
  }
  async function patchJson(url, body) {
    const r = await fetch(url, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    if (!r.ok) throw new Error(`PATCH ${r.status} @ ${url}`);
  }

  // 날짜 보정/검증
  function normalizeYmd(s) {
    if (!s) return null;
    s = String(s).trim();

    if (/^\d{2}-\d{2}-\d{2}$/.test(s)) {
      const [yy, mm, dd] = s.split('-');
      const y = parseInt(yy, 10);
      const century = y >= 70 ? '19' : '20';
      return `${century}${yy}-${mm}-${dd}`;
    }

    if (/^\d{4}-\d{2}-\d{2}/.test(s)) return s.substring(0, 10);

    const m = s.match(/^(\d{4})[\/.](\d{2})[\/.](\d{2})/);
    if (m) return `${m[1]}-${m[2]}-${m[3]}`;

    return null;
  }
  function toYmd(v) {
    return normalizeYmd(v);
  }
  function isYmd(s) {
    return typeof s === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(s);
  }
  function ymd(d) {
    const y = d.getFullYear(),
      m = String(d.getMonth() + 1).padStart(2, '0'),
      dd = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${dd}`;
  }
  function today() {
    return ymd(new Date());
  }
  function addDays(x, n) {
    const d = new Date(x);
    d.setDate(d.getDate() + n);
    return ymd(d);
  }
  function fmt(d) {
    return ymd(d);
  }
  function clamp01(p) {
    const n = Math.round(Number(p) || 0);
    return Math.max(0, Math.min(100, n));
  }

  // DTO -> Frappe Gantt 변환
  function toGanttTask(t) {
    const wfId = t.workflowsId ?? t.workflowId ?? null;
    let start = toYmd(t.startDate) || toYmd(t.dueDate) || today();
    let end = toYmd(t.dueDate) || addDays(start, 1);

    if (!isYmd(start)) start = today();
    if (!isYmd(end)) end = addDays(start, 1);
    if (new Date(end) < new Date(start)) end = addDays(start, 1);

    const indent = t.__level ? '  ' /*nbsp 두개*/ : '';
    const deps = t.__parentId ? String(t.__parentId) : '';

    return {
      id: String(t.id),
      name: indent + (t.title || '(제목 없음)'),
      start,
      end,
      progress: clamp01(t.progressPct),
      custom_class: wfId ? `wf-${wfId}` : 'wf-default',
      dependencies: deps, // 부모 → 자식 화살표
    };
  }

  // 워크플로우 색상 스타일 주입
  function injectWorkflowStyles(items) {
    if (!Array.isArray(items) || !items.length) return;

    const old = document.querySelector('style[data-gantt-wf-style]');
    if (old) old.remove();

    const st = document.createElement('style');
    st.setAttribute('data-gantt-wf-style', '');

    st.textContent = items
      .map(
        (w) => `
    .gantt .bar-wrapper.wf-${cssEsc(w.id)} .bar { fill: ${w.color || '#94a3b8'}; }
    .gantt .bar-wrapper.wf-${cssEsc(w.id)} .bar-progress { fill: ${w.color || '#94a3b8'}; opacity: .35; }
  `
      )
      .join('\n');

    document.head.appendChild(st);
  }

  // 메시지 렌더
  function renderMessage(wrap, text) {
    fitGanttHeight(wrap, 32);
    wrap.innerHTML = `<div style="padding:16px;color:#64748b">${escapeHtml(text)}</div>`;
  }

  // 문자열/셀렉터 이스케이프
  function escapeHtml(s) {
    return String(s).replace(/[&<>"']/g, (m) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]));
  }
  function cssEsc(v) {
    return String(v).replace(/[^a-zA-Z0-9_-]/g, '_');
  }

  // 자식 함수 deep
  async function loadTasksDeep(ctx, projectId, parentRows) {
    const out = [];
    for (const p of parentRows || []) {
      out.push({ ...p, __level: 0 }); // 부모
      if (p.childrenCount > 0) {
        try {
          const kids = await fetchJson(`${ctx}/api/projects/${projectId}/tasks/${p.id}/children`);
          for (const c of kids) {
            // 들여쓰기용 레벨과 의존선(부모→자식) 부여
            out.push({ ...c, __level: 1, __parentId: p.id });
          }
        } catch (e) {
          console.warn('[gantt] children load failed for', p.id, e);
        }
      }
    }
    return out;
  }

  // 공개
  global.initGantt = initGantt;
})(window);
