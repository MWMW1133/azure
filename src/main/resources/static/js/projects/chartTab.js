// /static/js/chartTab.js
(function (global) {
  async function initChartTab(ctx, projectId) {
    // 컨테이너 존재 + 레이아웃 보장
    const ids = ['graphByAssignee', 'graphByDueDate', 'graphByWorkflow'];
    await waitForContainers(ids, 40, 25);
    await ensureLaidOutByIds(ids, 30, 30);

    // ECharts 로드 보장
    await ensureEcharts();

    //   데이터
    const parents = await fetch(`${ctx}/api/projects/${projectId}/tasks`, { cache: 'no-cache' }).then((r) => r.json());
    const rows = await loadTasksDeep(ctx, projectId, parents);

    //   집계
    const byAssignee = countBy(rows, (t) => t.assigneeName || '미지정');
    const byDueDate = countBy(rows, (t) => normalizeYmd(t.dueDate), { excludeNull: true, sort: 'asc' });
    const byStatus = countBy(rows, (t) => t.workflowName || '미지정');

    //   렌더
    const charts = [];
    charts.push(renderBar('graphByAssignee', byAssignee, '직원 별 담당 태스크'));
    charts.push(renderBar('graphByDueDate', byDueDate, '태스크 마감일', { xLabelRotate: 45 }));
    charts.push(renderPie('graphByWorkflow', byStatus, '상태 별 태스크'));

    // 리사이즈
    window.addEventListener('resize', () => charts.forEach((c) => c?.resize()), { passive: true });
  }

  // 자식 태스크 로딩
  async function loadTasksDeep(ctx, projectId, parents) {
    const out = [];
    const seen = new Set();
    const MAX_DEPTH = 3;

    async function pushWithChildren(task, depth) {
      if (!task || seen.has(task.id)) return;
      seen.add(task.id);
      out.push(task);

      const count = Number(task.childrenCount || 0);
      if (depth >= MAX_DEPTH || count <= 0) return;

      try {
        const kids = await fetch(`${ctx}/api/projects/${projectId}/tasks/${task.id}/children`, { cache: 'no-cache' }).then((r) => r.json());
        for (const ch of kids || []) {
          await pushWithChildren(ch, depth + 1);
        }
      } catch (e) {
        console.warn('[chart] children load failed for', task.id, e);
      }
    }

    for (const p of parents || []) {
      await pushWithChildren(p, 0);
    }
    return out;
  }

  // ------- helpers -------
  async function ensureEcharts() {
    if (global.echarts) return;
    await new Promise((res, rej) => {
      const s = document.createElement('script');
      s.src = 'https://cdn.jsdelivr.net/npm/echarts@5/dist/echarts.min.js';
      s.async = true;
      s.onload = res;
      s.onerror = () => rej(new Error('ECharts load failed'));
      document.head.appendChild(s);
    });
  }
  async function waitForContainers(idList, tries = 40, delayMs = 25) {
    for (let i = 0; i < tries; i++) {
      const ok = idList.every((id) => document.getElementById(id) instanceof HTMLElement);
      if (ok) return true;
      await new Promise((r) => setTimeout(r, delayMs));
    }
    return false;
  }
  async function ensureLaidOutByIds(idList, tries = 30, delayMs = 30) {
    for (let i = 0; i < tries; i++) {
      const ok = idList.every((id) => {
        const el = document.getElementById(id);
        if (!(el instanceof HTMLElement)) return false;
        const r = el.getBoundingClientRect();
        return r.width >= 10 && r.height >= 10;
      });
      if (ok) return true;
      await new Promise((r) => requestAnimationFrame(() => requestAnimationFrame(r)));
      await new Promise((r) => setTimeout(r, delayMs));
    }
    return false;
  }

  function countBy(arr, keyFn, opts = {}) {
    const m = new Map();
    for (const it of arr || []) {
      const k0 = keyFn(it);
      if (opts.excludeNull && (k0 == null || k0 === '')) continue;
      const k = k0 ?? '미지정';
      m.set(k, (m.get(k) || 0) + 1);
    }
    let ent = [...m.entries()];
    if (opts.sort === 'asc') ent.sort((a, b) => String(a[0]).localeCompare(String(b[0])));
    if (opts.sort === 'desc') ent.sort((a, b) => String(b[0]).localeCompare(String(a[0])));
    return ent;
  }

  function normalizeYmd(s) {
    if (!s) return null;
    s = String(s).trim();
    if (/^\d{2}-\d{2}-\d{2}$/.test(s)) {
      const [yy, mm, dd] = s.split('-');
      const y = +yy;
      const c = y >= 70 ? '19' : '20';
      return `${c}${yy}-${mm}-${dd}`;
    }
    if (/^\d{4}-\d{2}-\d{2}/.test(s)) return s.slice(0, 10);
    const m = s.match(/^(\d{4})[\/.](\d{2})[\/.](\d{2})/);
    return m ? `${m[1]}-${m[2]}-${m[3]}` : s;
  }

  function renderBar(elId, data, title, opts = {}) {
    const el = document.getElementById(elId);
    if (!el) return null;
    if (el.__echarts__) {
      try {
        el.__echarts__.dispose();
      } catch {}
      el.__echarts__ = null;
    }
    const chart = echarts.init(el);
    el.__echarts__ = chart;
    chart.setOption({
      title: { text: title || '', left: 'center', top: 8, textStyle: { fontWeight: 700, fontSize: 14 } },
      grid: { left: 40, right: 10, top: 48, bottom: 40 },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.map((d) => d[0]), axisLabel: { rotate: opts.xLabelRotate || 0 } },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{ type: 'bar', data: data.map((d) => d[1]), barMaxWidth: 36, itemStyle: { borderRadius: [6, 6, 0, 0] } }],
    });
    return chart;
  }

  function renderPie(elId, data, title) {
    const el = document.getElementById(elId);
    if (!el) return null;
    if (el.__echarts__) {
      try {
        el.__echarts__.dispose();
      } catch {}
      el.__echarts__ = null;
    }
    const chart = echarts.init(el);
    el.__echarts__ = chart;
    chart.setOption({
      title: { text: title || '', left: 'center', top: 8, textStyle: { fontWeight: 700, fontSize: 14 } },
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', right: 10, top: 24 },
      series: [
        {
          type: 'pie',
          radius: ['45%', '70%'],
          center: ['35%', '56%'],
          label: { formatter: '{b}\n{c} ({d}%)' },
          data: data.map(([name, value]) => ({ name, value })),
        },
      ],
    });
    return chart;
  }

  // 전역 노출
  global.initChartTab = initChartTab;
})(window);
