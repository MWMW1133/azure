// ===== Assignee Panel (merged into taskRow.js) =====
(function mountAssigneePanel() {
  // 중복 마운트 가드 (버전으로)
  if (window.__ASSIGNEE_PANEL_MOUNTED__ === 'v3.1') return;
  window.__ASSIGNEE_PANEL_MOUNTED__ = 'v3.1';

  const root = document.getElementById('project-tab-root');
  if (!root) {
    console.warn('[assignee] #project-tab-root 없음');
    return;
  }
  const projectId = root.dataset.projectId;

  // --- API ---
  const api = {
    async searchMembers(q = '') {
      const r = await fetch(`/api/projects/${projectId}/members?query=${encodeURIComponent(q)}`, {
        credentials: 'same-origin',
      });
      if (!r.ok) throw new Error(`members ${r.status}`);
      return r.json();
    },
    async setAssignee(taskId, userId) {
      const r = await fetch(`/api/tasks/${taskId}/assignee`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify({ userId }), // null이면 해제
      });
      if (!r.ok) throw new Error(`assign ${r.status}`);
    },
  };

  // --- 상태 ---
  let activePanel = null;
  let panelOpenedAt = 0;

  // --- 셀 UI 갱신 ---
  function renderAssigneeCell(row, user) {
    const cell = row.querySelector('.assignee-cell');
    if (!cell) return;
    const panel = cell.querySelector('.assignee-panel');

    // 패널 외 요소 제거
    [...cell.childNodes].forEach((n) => {
      if (n === panel) return;
      if (n.nodeType === Node.ELEMENT_NODE) cell.removeChild(n);
    });

    if (user) {
      row.dataset.assigneeId = String(user.id);
      if (user.avatarUrl) {
        const img = document.createElement('img');
        img.className = 'assignee-img';
        img.src = user.avatarUrl;
        img.alt = user.name ?? '';
        cell.insertBefore(img, panel);
      } else {
        const div = document.createElement('div');
        div.className = 'assignee-initial';
        div.textContent = user.name?.[0] || 'U';
        cell.insertBefore(div, panel);
      }
    } else {
      row.dataset.assigneeId = '';
      const placeholder = document.createElement('div');
      placeholder.className = 'assignee-placeholder';
      placeholder.textContent = '-';
      cell.insertBefore(placeholder, panel);
    }
  }

  // --- 공통 핸들러 (캡처 전용) ---
  function handleDocClick(e) {
    // 좌클릭만
    if (e.type === 'click' && e.button !== 0) return;

    const inAssigneeArea = e.target.closest('.assignee-cell, .assignee-panel');
    if (inAssigneeArea) {
      e.__assigneeHandled = true;
    }

    if (e.target.closest('.assignee-panel')) {
      e.__assigneeHandled = true;
      return;
    }

    const cell = e.target.closest('.task-cell.assignee-cell');
    if (cell) {
      const panel = cell.querySelector('.assignee-panel');
      if (!panel) return;

      if (activePanel && activePanel !== panel) closePanel(activePanel);
      (panel.hidden ? openPanel : closePanel)(panel);

      e.stopImmediatePropagation();
      e.stopPropagation();
      return;
    }

    if (activePanel) {
      const justOpened = performance.now() - panelOpenedAt < 140;
      if (!justOpened && !activePanel.contains(e.target)) {
        closePanel(activePanel);
      }
    }
  }

  window.addEventListener('click', handleDocClick, true);
  document.addEventListener('click', handleDocClick, true);
  document.documentElement.addEventListener('click', handleDocClick, true);

  // ESC 닫기
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && activePanel) closePanel(activePanel);
  });

  function openPanel(panel) {
    panel.hidden = false;
    panel.classList.add('is-open');
    activePanel = panel;
    panelOpenedAt = performance.now();
    initPanel(panel);
  }
  function closePanel(panel) {
    panel.hidden = true;
    panel.classList.remove('is-open');
    if (activePanel === panel) activePanel = null;
  }

  function initPanel(panel) {
    const row = panel.closest('.task-row');
    const taskId = row?.dataset.taskId;
    if (!taskId) return;

    let currentAssigneeId = row.dataset.assigneeId || '';
    const list = panel.querySelector('.assignee-list');
    const searchInput = panel.querySelector('.assignee-search-input');
    const submitBtn = panel.querySelector('.assignee-submit');
    const unassignBtn = panel.querySelector('.assignee-unassign');
    let selectedUser = null;
    let debounceTimer;

    async function load(q = '') {
      const users = await api.searchMembers(q).catch(() => []);
      list.innerHTML = '';
      users.forEach((u) => {
        const isCurrent = String(u.id) === String(currentAssigneeId);
        const icon = isCurrent ? '×' : '+';
        const title = isCurrent ? '담당자 해제' : '배정';
        const li = document.createElement('li');
        li.className = 'assignee-item' + (isCurrent ? ' is-current' : '');
        li.dataset.id = String(u.id);
        li.innerHTML = `
          <div class="assignee-avatar">
            ${u.avatarUrl ? `<img src="${u.avatarUrl}" alt="">` : (u.name || 'U')[0]}
          </div>
          <div class="assignee-meta">
            <div class="assignee-name">${u.name ?? ''}</div>
            <div class="assignee-email">${u.email ?? ''}</div>
          </div>
          <button class="assignee-action" title="${title}" type="button" aria-label="${title}">
            ${icon}
          </button>
        `;
        list.appendChild(li);
      });
      selectedUser = null;
      if (submitBtn) submitBtn.disabled = true;
    }

    // 검색 입력
    if (searchInput) {
      searchInput.value = '';
      setTimeout(() => searchInput.focus(), 0);
      searchInput.oninput = (e) => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => load(e.target.value.trim()), 200);
      };
    }

    // 목록 클릭 (+ / × / 선택)
    list.onclick = async (e) => {
      console.log('클릭됨?');
      const item = e.target.closest('.assignee-item');
      console.log(e + '클릭됨');

      if (!item) return;
      const id = item.dataset.id;
      const actionBtn = e.target.closest('button.assignee-action');
      const isCurrent = String(id) === String(currentAssigneeId);

      if (!actionBtn) {
        // 버튼 아닌 영역: 선택만
        list.querySelectorAll('.assignee-item').forEach((li) => li.classList.remove('is-selected'));
        item.classList.add('is-selected');
        selectedUser = {
          id: Number(id),
          name: item.querySelector('.assignee-name')?.textContent || '',
          email: item.querySelector('.assignee-email')?.textContent || '',
          avatarUrl: item.querySelector('.assignee-avatar img')?.getAttribute('src') || null,
        };
        if (submitBtn) submitBtn.disabled = false;
        return;
      }

      // 버튼 눌렀을 때
      if (isCurrent) {
        // 즉시 해제
        try {
          await api.setAssignee(taskId, null);
          renderAssigneeCell(row, null);
          currentAssigneeId = '';
          await load(searchInput?.value || '');
          closePanel(panel);
        } catch (err) {
          console.warn('unassign failed', err);
          alert('담당자 해제에 실패했습니다.');
        }
      } else {
        // 선택만 표시 (배정은 Submit)
        list.querySelectorAll('.assignee-item').forEach((li) => li.classList.remove('is-selected'));
        item.classList.add('is-selected');
        selectedUser = {
          id: Number(id),
          name: item.querySelector('.assignee-name')?.textContent || '',
          email: item.querySelector('.assignee-email')?.textContent || '',
          avatarUrl: item.querySelector('.assignee-avatar img')?.getAttribute('src') || null,
        };
        if (submitBtn) submitBtn.disabled = false;
      }
    };

    // 배정 버튼
    if (submitBtn) {
      submitBtn.onclick = async () => {
        if (!selectedUser) return;
        try {
          await api.setAssignee(taskId, selectedUser.id); // 단일 담당자
          renderAssigneeCell(row, selectedUser);
          currentAssigneeId = String(selectedUser.id);
          await load(searchInput?.value || ''); // 아이콘 업데이트(+ → ×)
          closePanel(panel);
        } catch (err) {
          console.warn('assign failed', err);
          alert('담당자 배정에 실패했습니다.');
        }
      };
    }

    // 전용 해제 버튼이 따로 있으면
    if (unassignBtn) {
      unassignBtn.onclick = async () => {
        try {
          await api.setAssignee(taskId, null);
          renderAssigneeCell(row, null);
          currentAssigneeId = '';
          await load(searchInput?.value || '');
          closePanel(panel);
        } catch (err) {
          console.warn('unassign failed', err);
          alert('담당자 해제에 실패했습니다.');
        }
      };
    }

    // 초기 로드
    load('');
  }
})();
(function mountStatusPanel() {
  if (window.__STATUS_PANEL_MOUNTED__ === 'v2') return;
  window.__STATUS_PANEL_MOUNTED__ = 'v2';

  const root = document.getElementById('project-tab-root');
  if (!root) return;
  const projectId = root.dataset.projectId;

  const api = {
    async list() {
      const r = await fetch(`/api/projects/${projectId}/workflows`, { credentials: 'same-origin' });
      if (!r.ok) throw new Error('workflows list failed');
      return r.json(); // [{id,name,color, ...}]
    },
    async create(payload) {
      const r = await fetch(`/api/projects/${projectId}/workflows`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify(payload), // {name, color}만 넘겨도 됨
      });
      if (!r.ok) throw new Error('workflow create failed');
      return r.json();
    },
    async update(id, payload) {
      const r = await fetch(`/api/projects/${projectId}/workflows/${id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify(payload), // {name?, color?}
      });
      if (!r.ok) throw new Error('workflow update failed');
      return r.json();
    },
    async setTaskWorkflow(taskId, workflowId) {
      const r = await fetch(`/api/tasks/${taskId}/workflow`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify({ workflowId }),
      });
      if (!r.ok) throw new Error('set workflow failed');
    },
    async remove(id) {
      const r = await fetch(`/api/projects/${projectId}/workflows/${id}`, {
        method: 'DELETE',
        credentials: 'same-origin',
      });
      if (!r.ok) {
        const msg = r.status === 409 ? '이 상태를 사용하는 태스크가 있어 삭제할 수 없습니다.' : `삭제 실패 (${r.status})`;
        throw new Error(msg);
      }
    },
  };

  let activePanel = null;
  let openedAt = 0;

  function openPanel(panel) {
    panel.hidden = false;
    panel.classList.add('is-open');
    activePanel = panel;
    openedAt = performance.now();
    initPanel(panel);
  }
  function closePanel(panel) {
    panel.hidden = true;
    panel.classList.remove('is-open');
    if (activePanel === panel) activePanel = null;
  }

  // 캡처 단계 우선 처리 (다른 핸들러 간섭 방지)
  function handleClick(e) {
    // 패널 내부는 닫지 않음
    if (e.target.closest('.status-panel')) return;

    // 뱃지 클릭 → 패널 토글
    const badge = e.target.closest('.status-cell .status-badge');
    if (badge) {
      const cell = badge.closest('.status-cell');
      const panel = cell.querySelector('.status-panel');
      if (!panel) return;
      if (activePanel && activePanel !== panel) closePanel(activePanel);
      (panel.hidden ? openPanel : closePanel)(panel);
      e.stopImmediatePropagation();
      e.stopPropagation();
      return;
    }

    // 바깥 클릭 → 닫기
    if (activePanel) {
      const justOpened = performance.now() - openedAt < 140;
      if (!justOpened && !activePanel.contains(e.target)) closePanel(activePanel);
    }
  }
  window.addEventListener('click', handleClick, true);

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && activePanel) closePanel(activePanel);
  });

  function initPanel(panel) {
    const row = panel.closest('.task-row');
    const taskId = row?.dataset.taskId;
    if (!taskId) return;

    const listEl = panel.querySelector('.status-list');
    const searchEl = panel.querySelector('.status-search-input');
    const newNameEl = panel.querySelector('.status-new-name');
    const newColorEl = panel.querySelector('.status-new-color');
    const createBtn = panel.querySelector('.status-create-btn');
    const editToggleBtn = panel.querySelector('.status-edit-toggle');

    let all = [];
    let q = '';
    let editMode = false;

    function render() {
      const curId = row.dataset.workflowId || '';
      const filtered = q ? all.filter((w) => (w.name || '').toLowerCase().includes(q.toLowerCase())) : all;
      listEl.innerHTML = '';

      filtered.forEach((w) => {
        const li = document.createElement('li');
        li.className = 'status-list-item';
        li.dataset.id = String(w.id);

        if (!editMode) {
          li.innerHTML = `
            <span class="status-color-dot" style="background:${w.color || '#e5e7eb'}"></span>
            <span class="status-name">${w.name}</span>
            ${String(w.id) === String(curId) ? '<span class="status-current">현재</span>' : ''}
          `;
          li.onclick = async () => {
            try {
              await api.setTaskWorkflow(taskId, w.id);
              row.dataset.workflowId = String(w.id);
              const badge = row.querySelector('.status-cell .status-badge');
              if (badge) {
                badge.querySelector('.status-dot').style.background = w.color || '#e5e7eb';
                badge.querySelector('.status-text').textContent = w.name || '-';
              }
              closePanel(panel);
            } catch {
              alert('상태 변경 실패');
            }
          };
        } else {
          li.innerHTML = `
            <input class="status-edit-name" value="${w.name || ''}" />
            <input class="status-edit-color" type="color" value="${w.color || '#e5e7eb'}" />
            <button class="status-save-btn" title="저장">✔</button>
            <button class="status-delete-btn" title="삭제">X</button>
          `;
          const nameEl = li.querySelector('.status-edit-name');
          const colorEl = li.querySelector('.status-edit-color');

          li.querySelector('.status-save-btn').onclick = async (e) => {
            e.stopPropagation();
            const name = nameEl.value.trim();
            const color = colorEl.value.trim() || '#e5e7eb';
            if (!name) return;
            try {
              const updated = await api.update(w.id, { name, color });
              const idx = all.findIndex((x) => x.id === w.id);
              if (idx >= 0) all[idx] = { ...all[idx], name: updated.name, color: updated.color };
              render();
            } catch {
              alert('수정 실패');
            }
          };
          li.querySelector('.status-delete-btn').onclick = async (e) => {
            e.stopPropagation();
            // if () {
            //   alert('3개 이하로 ㄴㄴ연');
            //   return;
            // }
            if (!confirm(`'${w.name}' 상태를 삭제할까요?`)) return;
            try {
              await api.remove(w.id);
              // 목록에서 제거
              all = all.filter((x) => String(x.id) !== String(w.id));
              // 현재 행의 상태가 삭제된 것이면 뱃지 리셋
              const curId = row.dataset.workflowId || '';
              if (String(w.id) === String(curId)) {
                row.dataset.workflowId = '';
                const badge = row.querySelector('.status-cell .status-badge');
                if (badge) {
                  badge.querySelector('.status-dot').style.background = '#e5e7eb';
                  badge.querySelector('.status-text').textContent = '-';
                }
              }
              render();
            } catch (err) {
              alert(err.message || '삭제 실패');
            }
          };
        }

        listEl.appendChild(li);
      });
    }

    async function load() {
      all = await api.list().catch(() => []);
      render();
    }

    if (searchEl) {
      let t;
      searchEl.value = '';
      setTimeout(() => searchEl.focus(), 0);
      searchEl.oninput = (e) => {
        clearTimeout(t);
        t = setTimeout(() => {
          q = e.target.value.trim();
          render();
        }, 150);
      };
    }

    if (editToggleBtn) {
      editToggleBtn.onclick = () => {
        editMode = !editMode;
        render();
      };
    }

    if (createBtn) {
      createBtn.onclick = async () => {
        const name = (newNameEl?.value || '').trim();
        const color = (newColorEl?.value || '#e5e7eb').trim();
        if (!name) return;
        try {
          const created = await api.create({ name, color });
          all.push(created); // 서버에서 정렬/터미널 보정됨
          newNameEl.value = '';
          render();
        } catch {
          alert('상태 생성 실패');
        }
      };
    }

    load();
  }
})();
(function mountPriorityPanel() {
  if (window.__PRIORITY_PANEL_MOUNTED__ === 'v6') return;
  window.__PRIORITY_PANEL_MOUNTED__ = 'v6';

  // ===== 1) 5단계 고정 매핑 (id는 "문자" 키로 통일) =====
  const MAP_BY_ID = {
    5: { label: '매우 높음', color: '#ef4444', name: 'HIGHEST' },
    4: { label: '높음', color: '#f59e0b', name: 'HIGH' },
    3: { label: '보통', color: '#22c55e', name: 'MEDIUM' },
    2: { label: '낮음', color: '#3b82f6', name: 'LOW' },
    1: { label: '매우 낮음', color: '#64748b', name: 'LOWEST' },
  };
  // 서버/DB 영문명이 다를 수 있으니 별칭도 지원
  const NAME_ALIASES = {
    CRITICAL: 'HIGHEST',
    URGENT: 'HIGHEST',
    HIGHEST: 'HIGHEST',
    HIGH: 'HIGH',
    MEDIUM: 'MEDIUM',
    NORMAL: 'MEDIUM',
    LOW: 'LOW',
    LOWEST: 'LOWEST',
  };
  const ORDER_DESC = ['5', '4', '3', '2', '1'];
  const COLOR_FALLBACK = '#e5e7eb';

  // ===== 2) API =====
  const api = {
    async setTaskPriority(taskId, priorityId) {
      const r = await fetch(`/api/tasks/${taskId}/priority`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify({ priorityId }), // 서버와 키 통일
      });
      if (!r.ok) throw new Error('set priority failed: ' + r.status);
    },
  };

  // ===== 3) 유틸: 메타 해석 & 배지 그리기 =====
  function resolveMeta({ idKey, nameKey, textKey }) {
    // 1) id 우선
    if (idKey && MAP_BY_ID[idKey]) return MAP_BY_ID[idKey];
    // 2) data-priority-name (영문/별칭) 해석
    if (nameKey) {
      const alias = NAME_ALIASES[nameKey];
      if (alias) {
        const byAlias = Object.values(MAP_BY_ID).find((m) => m.name === alias);
        if (byAlias) return byAlias;
      }
    }
    // 3) 현재 텍스트(영문일 수 있음)로도 보정
    if (textKey) {
      const alias = NAME_ALIASES[textKey];
      if (alias) {
        const byTxt = Object.values(MAP_BY_ID).find((m) => m.name === alias);
        if (byTxt) return byTxt;
      }
    }
    return null;
  }

  function applyBadge(row) {
    const badge = row.querySelector('.priority-badge');
    if (!badge) return;

    const idKey = String(row.dataset.priorityId || row.dataset.priorityCode || '').trim(); // "1"~"5"
    const nameKey = (row.dataset.priorityName || '').trim().toUpperCase(); // 영문명
    const nowTxt = (badge.querySelector('.priority-text')?.textContent || '').trim().toUpperCase();

    const meta = resolveMeta({ idKey, nameKey, textKey: nowTxt });
    const text = meta ? meta.label : '-';
    const color = meta ? meta.color : COLOR_FALLBACK;

    const textEl = badge.querySelector('.priority-text');
    const dotEl = badge.querySelector('.priority-dot');
    if (textEl) textEl.textContent = text;
    if (dotEl) dotEl.style.background = color;
  }

  async function setFromPanel(row, idStr) {
    const m = MAP_BY_ID[String(idStr)];
    row.dataset.priorityId = String(idStr); // "1"~"5"
    row.dataset.priorityName = m?.name || ''; // 영문 표준명 보관
    applyBadge(row); // 즉시 배지 갱신
  }

  // ===== 4) 패널 열기/닫기 + 토글 핸들러 =====
  let activePanel = null,
    openedAt = 0;

  function openPanel(panel) {
    panel.hidden = false;
    panel.classList.add('is-open');
    activePanel = panel;
    openedAt = performance.now();
    initPanel(panel);
  }
  function closePanel(panel) {
    panel.hidden = true;
    panel.classList.remove('is-open');
    if (activePanel === panel) activePanel = null;
  }

  function handleClick(e) {
    if (e.target.closest('.priority-panel')) return; // 내부 클릭은 무시

    const badge = e.target.closest('.priority-cell .priority-badge');
    if (badge) {
      const cell = badge.closest('.priority-cell');
      const panel = cell.querySelector('.priority-panel');
      if (!panel) return;
      if (activePanel && activePanel !== panel) closePanel(activePanel);
      (panel.hidden ? openPanel : closePanel)(panel);
      e.stopImmediatePropagation();
      e.stopPropagation();
      return;
    }

    if (activePanel) {
      const justOpened = performance.now() - openedAt < 140;
      if (!justOpened && !activePanel.contains(e.target)) closePanel(activePanel);
    }
  }
  window.addEventListener('click', handleClick, true);
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && activePanel) closePanel(activePanel);
  });

  // ===== 5) 패널 초기화 (리스트 생성 & 클릭 → PATCH) =====
  async function initPanel(panel) {
    const row = panel.closest('.task-row');
    const taskId = row?.dataset.taskId;
    if (!taskId) return;

    const listEl = panel.querySelector('.priority-list');
    if (!listEl) return;
    listEl.innerHTML = '';

    const currentId = String(row.dataset.priorityId || row.dataset.priorityCode || '');

    ORDER_DESC.forEach((id) => {
      const m = MAP_BY_ID[id];
      const li = document.createElement('li');
      li.className = 'priority-list-item';
      li.dataset.id = id;
      li.innerHTML = `
        <span class="priority-color-dot" style="background:${m.color}"></span>
        <span class="priority-name">${m.label}</span>
        ${id === currentId ? '<span class="priority-current">현재</span>' : ''}
      `;
      li.onclick = async () => {
        try {
          await api.setTaskPriority(taskId, Number(id)); // 서버는 숫자 사용
          await setFromPanel(row, id); // data-* 업데이트 + 배지 즉시 갱신
          closePanel(panel);
        } catch (err) {
          console.warn(err);
          alert('우선순위 변경 실패');
        }
      };
      listEl.appendChild(li);
    });
  }

  // ===== 6) 초기/동적 렌더 모두 커버 =====
  function applyAll() {
    document.querySelectorAll('.task-row').forEach(applyBadge);
  }
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', applyAll, { once: true });
  } else {
    applyAll(); // DOM 이미 준비된 경우
  }

  // Ajax로 추가되는 행도 자동 반영
  const mo = new MutationObserver((muts) => {
    let touched = false;
    muts.forEach((m) => {
      m.addedNodes &&
        m.addedNodes.forEach((n) => {
          if (n.nodeType !== 1) return;
          if (n.matches && n.matches('.task-row')) touched = true;
          else if (n.querySelector && n.querySelector('.task-row')) touched = true;
        });
    });
    if (touched) applyAll();
  });
  mo.observe(document.body, { childList: true, subtree: true });

  // 필요 시 외부에서 강제 재적용할 수 있게 훅 제공
  window.__applyAllPriorityBadges__ = applyAll;
})();

/* --------------------- 수정내역(Audit) 모달 --------------------- */
(function initAuditUI() {
  // 이미 초기화 됐으면 재초기화 방지
  if (window.__AUDIT_UI_INIT__) return;
  window.__AUDIT_UI_INIT__ = true;

  // 안전용 esc
  if (typeof window.esc !== 'function') {
    window.esc = function (s) {
      if (s == null) return '';
      return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    };
  }

  // 네임스페이스
  const AUD = (window.__AUDIT__ = window.__AUDIT__ || {});

  // 설정 (이미 있으면 재사용해서 "already been declared" 회피)
  AUD.HIDDEN_KEYS = AUD.HIDDEN_KEYS || new Set(['assigneeId', 'workflowId', 'priorityId', 'workflowColor', 'fileId']);

  AUD.LABEL_MAP = AUD.LABEL_MAP || {
    assigneeName: '담당자',
    workflowName: '상태',
    priorityName: '우선순위',
    fileName: '파일명',
    // 그 외 키는 원래 키 그대로 라벨 처리
  };

  // before/after 정규화
  AUD.ba =
    AUD.ba ||
    function (v) {
      if (!v || typeof v !== 'object') return { before: null, after: null };
      return {
        before: v.before ?? v.beforeValue ?? v.old ?? v.prev ?? null,
        after: v.after ?? v.afterValue ?? v.new ?? v.next ?? null,
      };
    };

  // 값 포맷
  AUD.fmtValue =
    AUD.fmtValue ||
    function (x) {
      if (x === null || x === undefined || String(x).trim() === '') return '<em>-</em>';
      return esc(String(x));
    };

  // 액션 한글화
  AUD.humanizeAction =
    AUD.humanizeAction ||
    function (a) {
      const map = {
        ASSIGNEE_CHANGED: '담당자 변경',
        WORKFLOW_CHANGED: '상태 변경',
        PRIORITY_CHANGED: '우선순위 변경',
        FILE_ATTACHED: '파일 첨부',
        FILE_REMOVED: '파일 제거',
      };
      return map[a] || a;
    };

  // 시간 포맷
  AUD.formatKST =
    AUD.formatKST ||
    function (iso) {
      try {
        const d = new Date(iso);
        if (isNaN(d.getTime())) return iso;
        return d.toLocaleString(); // 필요시 'ko-KR', { timeZone:'Asia/Seoul' }
      } catch {
        return iso;
      }
    };

  AUD.PRIORITY_KO = AUD.PRIORITY_KO || {
    lowest: '매우 낮음',
    low: '낮음',
    normal: '보통',
    medium: '보통',
    mid: '보통',
    high: '높음',
    highest: '매우 높음',
  };

  AUD.translateValue =
    AUD.translateValue ||
    function (key, val) {
      if (val == null) return val;
      if (key === 'priorityName') {
        const k = String(val).trim().toLowerCase();
        return AUD.PRIORITY_KO[k] || val;
      }
      return val;
    };

  // diff 렌더
  AUD.renderDiff = function (changes) {
    if (!changes || typeof changes !== 'object') return '';
    const rows = Object.entries(changes)
      .filter(([k]) => !AUD.HIDDEN_KEYS.has(k))
      .map(([k, v]) => {
        const { before, after } = AUD.ba(v);
        // 3) priorityName 값 한글 변환
        const beforeT = AUD.translateValue(k, before);
        const afterT = AUD.translateValue(k, after);

        const label = esc(AUD.LABEL_MAP[k] || k);
        const beforeV = AUD.fmtValue(beforeT);
        const afterV = AUD.fmtValue(afterT);
        return `
          <dt>${label}</dt>
          <dd><div class="fromto">${beforeV} → ${afterV}</div></dd>
        `;
      })
      .join('');
    return rows ? `<div class="audit-diff"><dl>${rows}</dl></div>` : '';
  };

  // 아이템 렌더
  AUD.renderAuditItem = function (item) {
    const ts = esc(AUD.formatKST(item.createdAt || item.created_at || ''));
    const action = esc(AUD.humanizeAction(item.action || ''));

    let changes = {};
    try {
      changes = item.diffJson ? JSON.parse(item.diffJson) : {};
    } catch (e) {
      console.warn('invalid diffJson', e);
    }
    const diffHtml = AUD.renderDiff(changes);

    // 작성자 ID는 표시하지 않음
    return `
      <div class="audit-item">
        <div class="audit-meta">${ts} · ${action}</div>
        ${diffHtml}
      </div>
    `;
  };

  // 모달 열기
  AUD.openModal = async function (taskId) {
    const modal = document.getElementById('audit-modal');
    if (!modal) return console.warn('[audit] #audit-modal not found');
    const listEl = modal.querySelector('.audit-list');
    if (!listEl) return console.warn('[audit] .audit-list not found');

    listEl.innerHTML = '불러오는 중...';

    try {
      const res = await fetch(`/api/tasks/${taskId}/audits`, { credentials: 'same-origin' });
      if (!res.ok) throw new Error(`audit api fail (${res.status})`);
      const items = await res.json();
      listEl.innerHTML = items && items.length ? items.map(AUD.renderAuditItem).join('') : '<div>내역이 없습니다.</div>';
    } catch (err) {
      console.error(err);
      listEl.innerHTML = '<div>수정 내역을 불러오지 못했습니다.</div>';
    }

    modal.classList.remove('hidden');
    modal.setAttribute('aria-hidden', 'false');

    const onClose = () => {
      modal.classList.add('hidden');
      modal.setAttribute('aria-hidden', 'true');
    };
    const closeBtn = modal.querySelector('.audit-modal__close');
    const backdrop = modal.querySelector('.audit-modal__backdrop');
    if (closeBtn) closeBtn.onclick = onClose;
    if (backdrop) backdrop.onclick = onClose;

    const escClose = (ev) => {
      if (ev.key === 'Escape') {
        onClose();
        document.removeEventListener('keydown', escClose);
      }
    };
    document.addEventListener('keydown', escClose);
  };

  // ‘최근 수정일’ 셀 클릭 → 모달
  document.addEventListener('click', function (e) {
    const cell = e.target.closest('.task-row .updated-at-cell');
    if (!cell) return;
    const row = cell.closest('.task-row');
    const taskId = row?.dataset.taskId;
    if (!taskId) return;
    AUD.openModal(taskId);
  });
})();
