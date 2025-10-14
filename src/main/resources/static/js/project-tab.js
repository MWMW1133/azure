(() => {
  const $ = (sel, root = document) => root.querySelector(sel);
  const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));
  const main = $('.project-body');

  // ------- 컨텍스트/경로 유틸 -------
  const rootEl = document.getElementById('project-tab-root') || document.getElementById('project-tab-view-root'); //수정
  const APP_CONTEXT = (rootEl?.dataset.contextPath || window.APP_CONTEXT || '').replace(/\/$/, '');
  const apiUrl = (p) => `${APP_CONTEXT}${p}`;

  // ------- 프로젝트 컨텍스트 -------
  const PROJECT_ID = (rootEl?.dataset.projectId || window.PROJECT_ID || '').trim();
  const PROJECT_NAME = (rootEl?.dataset.projectName || window.PROJECT_NAME || '').trim();
  if (!PROJECT_ID) console.warn('[project] PROJECT_ID is empty.');

  // ------- API 래퍼 -------
  const api = {
    async getProject() {
      const r = await fetch(apiUrl(`/api/projects/${PROJECT_ID}`), { cache: 'no-cache' });
      if (!r.ok) throw new Error(`getProject ${r.status}`);
      return r.json();
    },
    async getTags() {
      const r = await fetch(apiUrl(`/api/projects/${PROJECT_ID}/tags`), { cache: 'no-cache' });
      if (!r.ok) throw new Error(`getTags ${r.status}`);
      return r.json();
    },
    async addTag(name) {
      const r = await fetch(apiUrl(`/api/projects/${PROJECT_ID}/tags`), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name }),
      });
      if (!r.ok) throw new Error(`addTag ${r.status}`);
      return r.json?.() ?? true;
    },
    async removeTag(tagId) {
      const r = await fetch(apiUrl(`/api/projects/${PROJECT_ID}/tags/${encodeURIComponent(tagId)}`), { method: 'DELETE' });
      if (!r.ok) throw new Error(`removeTag ${r.status}`);
      return true;
    },
    async searchUsers(q = '') {
      const r = await fetch(apiUrl(`/api/users?query=${encodeURIComponent(q)}`), { cache: 'no-cache' });
      if (!r.ok) throw new Error(`searchUsers ${r.status}`);
      return r.json();
    },
    async invite(userIds) {
      const r = await fetch(apiUrl(`/api/projects/${PROJECT_ID}/invitations`), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userIds }),
      });
      if (!r.ok) throw new Error(`invite ${r.status}`);
      return true;
    },
  };

  // ------- 외부 리소스 로더(중복 방지) -------
  function loadScriptOnce(src) {
    return new Promise((resolve, reject) => {
      if ([...document.scripts].some(s => (s.src || '').includes(src))) return resolve();
      const s = document.createElement('script');
      s.src = src;
      s.onload = resolve;
      s.onerror = reject;
      document.body.appendChild(s);
    });
  }
  function loadCssOnce(href) {
    if ([...document.querySelectorAll('link[rel="stylesheet"]')].some(l => (l.href || '').includes(href))) return;
    const l = document.createElement('link');
    l.rel = 'stylesheet';
    l.href = href;
    document.head.appendChild(l);
  }
  async function ensureFullCalendarLoaded() {
    loadCssOnce('fullcalendar@6.1.15/index.global.min.css');
    if (!window.FullCalendar) {
      await loadScriptOnce('https://cdn.jsdelivr.net/npm/fullcalendar@6.1.15/index.global.min.js');
    }
  }

  // ------- Router -------
  const Router = {
    go(name) {
      const projectId = PROJECT_ID;
      const ctx = APP_CONTEXT;

      const map = {
        table:    `${ctx}/projects/${projectId}/table`,
        card:     `${ctx}/projects/${projectId}/card`,
        gantt:    `${ctx}/projects/${projectId}/gantt`,
        chart:    `${ctx}/projects/${projectId}/chart`,
        calendar: `${ctx}/projects/${projectId}/calendar`,
        files:    `${ctx}/projects/${projectId}/files`,
        members:  `${ctx}/projects/${projectId}/members`,
      };

      const url = map[name];
      if (!url) return render('<h1>Not Found</h1>');

      fetch(url, { cache: 'no-cache' })
        .then((r) => r.text())
        .then((html) => {
          render(html);

          // ====== 간트 ======
          if (name === 'gantt' && typeof window.initGantt === 'function') {
            raf2(() => {
              const sel = '.project-body #gantt-wrap';
              if (!document.querySelector(sel)) {
                console.warn('[GANTT] container not found yet');
                return;
              }
              if (typeof Gantt === 'undefined') {
                console.error('[GANTT] library not loaded');
                return;
              }
              window.initGantt(APP_CONTEXT, PROJECT_ID, { container: sel });
            });
          }

          // ====== 차트 ======
          if (name === 'chart') {
            raf2(() => {
              if (typeof window.initChartTab === 'function') {
                window.initChartTab(APP_CONTEXT, PROJECT_ID);
              } else {
                console.error('[CHART] initChartTab is not loaded');
              }
            });
          }

          // ====== 캘린더 ======
          if (name === 'calendar') {
            // FullCalendar 리소스 보장 후 프래그먼트 내 init 호출
            ensureFullCalendarLoaded()
              .then(() => {
                raf2(() => {
                  if (typeof window.initProjectCalendar === 'function') {
                    window.initProjectCalendar(); // 내부에 중복가드 있음
                  } else {
                    // 혹시 프래그먼트가 인라인 IIFE만 갖고 있어도 한 프레임 더 밀어 실행 기회를 줌
                    setTimeout(() => {
                      if (typeof window.initProjectCalendar === 'function') {
                        window.initProjectCalendar();
                      }
                    }, 0);
                  }
                });
              })
              .catch(err => console.error('[CAL] FC load failed', err));
          }
        })
        .catch((err) => {
          console.error('[Router] error:', err);
          render('<h1>Load Error</h1>');
        });
    },
  };

  function render(html) {
    if (!main) return;

    // 1) 프래그먼트 파싱
    const tmp = document.createElement('div');
    tmp.innerHTML = html;

    // 2) <script>와 <link rel="stylesheet"> 분리
    const scripts = Array.from(tmp.querySelectorAll('script'));
    const links   = Array.from(tmp.querySelectorAll('link[rel="stylesheet"]'));
    scripts.forEach(s => s.parentNode.removeChild(s));
    links.forEach(l => l.parentNode && l.parentNode.removeChild(l));

    // 3) 본문 교체 (스크립트/링크 제외)
    main.innerHTML = '';
    while (tmp.firstChild) main.appendChild(tmp.firstChild);

    // 4) 스타일시트는 <head>에(중복 방지)
    const head = document.head;
    links.forEach(l => {
      const href = l.getAttribute('href') || '';
      const already = Array.from(document.querySelectorAll('link[rel="stylesheet"]'))
        .some(x => (x.getAttribute('href') || '') === href);
      if (!already) {
        const el = document.createElement('link');
        el.rel = 'stylesheet';
        el.href = href;
        el.media = l.media || '';
        head.appendChild(el);
      }
    });

    // 5) 스크립트 실행 (중복 방지)
    const existingSrcs = new Set(Array.from(document.scripts).map(s => s.src));
    scripts.forEach(s => {
      const src = s.getAttribute('src');
      const type = s.getAttribute('type') || '';
      const asyncAttr = s.hasAttribute('async');
      const deferAttr = s.hasAttribute('defer');

      const el = document.createElement('script');
      if (type) el.type = type;

      if (src) {
        const abs = src; // 상대/절대 그대로
        if (existingSrcs.has(abs)) return; // 이미 로드된 동일 src 는 건너뜀
        el.src = abs;
        if (asyncAttr) el.async = true;
        if (deferAttr) el.defer = true;
        document.body.appendChild(el);
      } else {
        el.text = s.textContent || '';
        document.body.appendChild(el);
      }
    });
  }

  // ------- Header(제목/태그) -------
  async function renderHeader() {
    try {
      const titleEl = document.querySelector('.project-title');
      const tagsWrap = document.querySelector('.project-tags');
      if (titleEl) titleEl.textContent = PROJECT_NAME || '';
      if (!tagsWrap) return;

      const tags = await api.getTags();
      tagsWrap.innerHTML = '';
      for (const t of tags) {
        const span = document.createElement('span');
        span.className = 'tag';
        span.textContent = `#${t.name}`;
        tagsWrap.appendChild(span);
      }
    } catch (err) {
      console.warn('[renderHeader] failed:', err);
    }
  }

  // ------- Tabs -------
  function setActiveTab(btn) {
    $$('.project-tab').forEach((b) => {
      const active = b === btn;
      b.classList.toggle('is-active', active);
      b.setAttribute('aria-selected', active ? 'true' : 'false');
    });
  }
  function bindTabs() {
    $$('.project-tab').forEach((btn) => {
      btn.addEventListener('click', () => {
        Router.go(btn.dataset.view);
        setActiveTab(btn);
      });
    });
    const first = $('.project-tab[data-view]');
    if (first) {
      setActiveTab(first);
      Router.go(first.dataset.view);
    }
  }

  // ------- Popover(태그) -------
  function createTagPopoverContent() {
    const wrap = document.createElement('div');
    wrap.className = 'p-2';
    wrap.innerHTML = `
      <input id="tag-input" class="form-control" placeholder="태그를 입력하세요." />
      <div id="tag-list" class="tag-popover-list"></div>
    `;
    return wrap;
  }

  async function refreshTagList(container) {
    const listEl = container.querySelector('#tag-list');
    if (!listEl) return;
    const tags = await api.getTags();
    listEl.innerHTML = '';
    tags.forEach((t) => {
      const chip = document.createElement('span');
      chip.className = 'tag-chip';
      chip.innerHTML = `<span>#${t.name}</span><button class="tag-remove" title="삭제" aria-label="삭제" data-id="${t.id}">×</button>`;
      listEl.appendChild(chip);
    });
  }

  function initTagPopover() {
    const el = document.getElementById('addTagBtn');
    if (!el || !window.bootstrap) return;
    const exist = bootstrap.Popover.getInstance(el);
    if (exist) return;

    new bootstrap.Popover(el, {
      trigger: 'click',
      placement: 'bottom',
      html: true,
      container: 'body',
      customClass: 'glass-popover',
      sanitize: false,
      content: createTagPopoverContent,
      popperConfig: { modifiers: [{ name: 'offset', options: { offset: [0, 10] } }] },
      title: '태그 추가',
    });

    el.addEventListener('shown.bs.popover', async () => {
      const popEl = document.querySelector('.popover .popover-body');
      if (!popEl) return;
      await refreshTagList(popEl);

      const input = popEl.querySelector('#tag-input');
      input?.focus();

      input?.addEventListener('keydown', async (e) => {
        if (e.key !== 'Enter') return;
        const v = input.value.trim();
        if (!v) return;
        try {
          await api.addTag(v);
          input.value = '';
          await refreshTagList(popEl);
          renderHeader();
        } catch (err) {
          console.warn('addTag failed', err);
        }
      });

      popEl.addEventListener('click', async (e) => {
        const btn = e.target.closest('.tag-remove');
        if (!btn) return;
        try {
          await api.removeTag(btn.dataset.id);
          await refreshTagList(popEl);
          renderHeader();
        } catch (err) {
          console.warn('removeTag failed', err);
        }
      });
    });

    document.addEventListener('click', (e) => {
      const popEl = document.querySelector('.popover');
      const instance = bootstrap.Popover.getInstance(el);
      if (!popEl || !instance) return;
      const inside = el.contains(e.target) || popEl.contains(e.target);
      if (!inside) instance.hide();
    });
  }

  // ------- Invite Panel -------
  function initInvitePanel() {
    const openBtn = $('#project-add-member');
    const panel = $('#invite-panel');
    const list = $('#invite-list');
    const searchInput = $('#invite-search-input');
    const submitBtn = $('#invite-submit');
    if (!openBtn || !panel) return;

    const selected = new Set();

    async function loadList(q = '') {
      const users = await api.searchUsers(q);
      list.innerHTML = '';
      users.forEach((u) => {
        const li = document.createElement('li');
        li.className = 'invite-item' + (selected.has(u.id) ? ' is-selected' : '');
        li.dataset.id = u.id;
        li.innerHTML = `
          <div class="invite-avatar">
            ${u.avatarUrl ? `<img src="${u.avatarUrl}" alt="">` : (u.name || 'U')[0]}
          </div>
          <div class="invite-meta">
            <div class="invite-name">${u.name}</div>
            <div class="invite-email">${u.email}</div>
          </div>
          <button class="invite-action" title="${selected.has(u.id) ? '선택됨' : '추가'}">${selected.has(u.id) ? '✓' : '+'}</button>
        `;
        list.appendChild(li);
      });
      submitBtn.disabled = selected.size === 0;
    }

    openBtn.addEventListener('click', () => {
      const nameEl = $('#invite-project-name');
      if (nameEl) nameEl.textContent = PROJECT_NAME || '';
      panel.classList.add('is-open');
      loadList('');
      if (searchInput) {
        searchInput.value = '';
        searchInput.focus();
      }
    });

    document.addEventListener('click', (e) => {
      if (!panel.classList.contains('is-open')) return;
      const inside = panel.contains(e.target) || openBtn.contains(e.target);
      if (!inside) panel.classList.remove('is-open');
    });

    let t;
    searchInput?.addEventListener('input', (e) => {
      clearTimeout(t);
      t = setTimeout(() => loadList(e.target.value.trim()), 200);
    });

    list?.addEventListener('click', (e) => {
      const item = e.target.closest('.invite-item');
      if (!item) return;
      const id = item.dataset.id;
      if (selected.has(id)) selected.delete(id);
      else selected.add(id);
      item.classList.toggle('is-selected');
      item.querySelector('.invite-action').textContent = selected.has(id) ? '✓' : '+';
      submitBtn.disabled = selected.size === 0;
    });

    submitBtn?.addEventListener('click', async () => {
      if (selected.size === 0) return;
      try {
        await api.invite([...selected]);
        selected.clear();
        panel.classList.remove('is-open');
        submitBtn.disabled = true;
      } catch (err) {
        console.warn('invite failed', err);
      }
    });
  }

  function raf2(fn) {
    const raf =
      window.requestAnimationFrame ||
      window.webkitRequestAnimationFrame ||
      window.mozRequestAnimationFrame ||
      (cb => setTimeout(cb, 0));     // 최후 폴백

    raf(() => raf(fn));
  }

  async function waitForContainers(idList, tries = 40, delayMs = 25) {
    for (let i = 0; i < tries; i++) {
      const ok = idList.every((id) => document.getElementById(id));
      if (ok) return true;
      await new Promise((r) => setTimeout(r, delayMs));
    }
    return false;
  }

  // ------- boot -------
  window.addEventListener('DOMContentLoaded', () => {
    renderHeader();
    bindTabs();
    initTagPopover();
    initInvitePanel();
  });
})();
