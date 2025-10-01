(() => {
  const $ = (sel, root = document) => root.querySelector(sel);
  const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));
  const main = $('.project-body');

  // ------- 컨텍스트/경로 유틸 -------
  const rootEl = document.getElementById('project-root');
  const APP_CONTEXT = (rootEl?.dataset.contextPath || window.APP_CONTEXT || '').replace(/\/$/, '');
  const apiUrl = (p) => `${APP_CONTEXT}${p}`;

  // ------- 프로젝트 컨텍스트 -------
  const PROJECT_ID = (rootEl?.dataset.projectId || window.PROJECT_ID || '').trim();
  const PROJECT_NAME = (rootEl?.dataset.projectName || window.PROJECT_NAME || '').trim();
  if (!PROJECT_ID) console.warn('[project] PROJECT_ID is empty. Check JSP data-project-id or window.PROJECT_ID');

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

  // ------- Router -------
  const Router = {
    go(name) {
      const base = `/projects/${encodeURIComponent(PROJECT_ID)}`;
      const map = {
        table: apiUrl(`${base}/table`),
        card: apiUrl(`${base}/card`),
        gantt: apiUrl(`${base}/gantt`),
        chart: apiUrl(`${base}/chart`),
        calendar: apiUrl(`${base}/calendar`),
        files: apiUrl(`${base}/files`),
        members: apiUrl(`${base}/members`),
      };
      const url = map[name];
      if (!url) return render('<h1>Not Found</h1>');
      fetch(url, { cache: 'no-cache' })
        .then((r) => r.text())
        .then(render)
        .catch(() => render('<h1>Load Error</h1>'));
    },
  };

  function render(html) {
    if (main) main.innerHTML = html;
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

  // ------- boot -------
  window.addEventListener('DOMContentLoaded', () => {
    renderHeader();
    bindTabs();
    initTagPopover();
    initInvitePanel();
  });
})();
