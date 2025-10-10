document.addEventListener('DOMContentLoaded', function () {
  // ===== 중복 마운트 가드 =====
  if (window.__PROJECT_TAB_MOUNTED) return;
  window.__PROJECT_TAB_MOUNTED = true;

  const root = document.getElementById('project-tab-root');
  const ctx = (root?.dataset.ctx || '').replace(/\/$/, '');
  const projectId = root?.dataset.projectId;

  const API = {
    createTask: `${ctx}/api/projects/${projectId}/tasks`,
    listTasks: `${ctx}/api/projects/${projectId}/tasks`,
    bulkDelete: `${ctx}/api/projects/${projectId}/tasks/bulk-delete`,
    workflows: `${ctx}/api/projects/${projectId}/workflows`,
    assign: (taskId) => `${ctx}/api/tasks/${taskId}/assignee`,
    setWorkflow: (taskId) => `${ctx}/api/tasks/${taskId}/workflow`,
    children: (parentId) => `${ctx}/api/projects/${projectId}/tasks/${parentId}/children`,
    users: `${ctx}/api/users`,
  };

  let currentOpenForm = null;
  let activeStatusPopover = null;

  // === 더블클릭 후 잔여 click 무시용 타임스탬프 ===
  let suppressClickUntil = 0;

  // ---------- 유틸 ----------
  const esc = (s) => (s == null ? '' : String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;'));
  const showEl = (el) => {
    if (!el) return;
    el.classList.remove('hidden');
    el.hidden = false;
    el.removeAttribute('hidden');
  };
  const hideEl = (el) => {
    if (!el) return;
    el.classList.add('hidden');
    el.hidden = true;
  };
  const isHidden = (el) => !el || el.classList.contains('hidden') || el.hidden === true || el.hasAttribute('hidden');

  function updateAllProgressBars() {
    document.querySelectorAll('.task-progress-bar').forEach((bar) => {
      const progress = parseInt(bar.dataset.progress || '0', 10);
      bar.style.width = Math.max(0, Math.min(100, progress)) + '%';
    });
  }

  //하위태스크 생성
  function renderChildRow(t) {
    const hasChildren = !!(t.childrenCount && Number(t.childrenCount) > 0);

    // --- assignee robust 파싱 ---
    const assigneeObj = t.assignee && typeof t.assignee === 'object' ? t.assignee : null;

    const assigneeId = t.assigneeId ?? assigneeObj?.id ?? null;

    const assigneeName = t.assigneeName ?? assigneeObj?.name ?? (typeof t.assignee === 'string' ? t.assignee : '') ?? '';

    const avatarUrl = t.assigneeAvatarUrl ?? assigneeObj?.avatarUrl ?? null;

    const workflowId = t.workflowId ?? (t.workflow && t.workflow.id) ?? '';
    const priorityId = t.priorityId ?? (t.priority && t.priority.id) ?? '';
    const pct = t.progressPct ?? 0;
    const firstInitial = (s) => (s && s.length ? s[0] : 'U');

    // 👉 표시 조건을 assigneeId "또는" (assigneeName/아바타)로 완화
    const hasAssigneeVisual = !!(assigneeId || assigneeName || avatarUrl);

    return `
    <div class="task-row sub"
         data-task-id="${t.id}"
         data-assignee-id="${assigneeId ?? ''}"
         data-workflow-id="${workflowId}"
         data-priority-id="${priorityId}"
         data-progress="${pct}">
      <div class="task-cell task-actions-cell">
        <div class="icon-wrapper">
          ${hasChildren ? '<span class="toggle-icon js-toggle-subtasks"><i class="fa-solid fa-caret-right"></i></span>' : '<input type="checkbox" class="form-check-input">'}
        </div>
      </div>

      <div class="task-cell task-title-cell">${esc(t.title)}</div>

      <div class="task-cell assignee-cell" data-cell="assignee">
        ${
          hasAssigneeVisual
            ? avatarUrl
              ? `<img src="${avatarUrl}" class="assignee-img" alt="${esc(assigneeName)}" />`
              : `<div class="assignee-initial">${esc(firstInitial(assigneeName))}</div>`
            : `<div class="assignee-placeholder">-</div>`
        }
        <div class="assignee-panel" role="dialog" aria-modal="true" hidden>
          <div class="assignee-search">
            <i class="bi bi-search"></i>
            <input type="text" class="assignee-search-input" placeholder="검색" aria-label="사용자 검색" />
          </div>
          <ul class="assignee-list" aria-label="사용자 목록"></ul>
          <div class="assignee-footer">
            <button type="button" class="assignee-submit" disabled>
              <i class="bi bi-plus-lg"></i> 배정하기
            </button>
          </div>
        </div>
      </div>

      <div class="task-cell started-at-cell">${esc(t.startDate ?? '')}</div>
      <div class="task-cell duedate-cell">${esc(t.dueDate ?? '')}</div>
      <div class="task-cell status-cell" data-cell="workflow">
        <button type="button" class="status-badge" data-workflow-id="${t.workflowId ?? ''}">
          <span class="status-dot" style="background:${esc(t.workflowColor || '#e5e7eb')}"></span>
          <span class="status-text">${esc(t.workflowName || '-')}</span>
        </button>
        <div class="status-panel" role="dialog" aria-modal="true" hidden>
          <div class="status-search">
            <i class="bi bi-search"></i>
            <input type="text" class="status-search-input" placeholder="상태 검색" />
          </div>
          <ul class="status-list" aria-label="워크플로 목록"></ul>
          <div class="status-footer">
            <input class="status-new-name" type="text" placeholder="새 상태 이름" />
            <input class="status-new-color" type="color" value="#e5e7eb" />
            <button type="button" class="status-create-btn">추가</button>
          </div>
        </div>
      </div>
      <div class="task-cell priority-cell">${esc(t.priority ?? '')}</div>
      <div class="task-cell progress-cell">
        <div class="progress-cell-wrapper">
          <span class="progress-value">${esc(pct)}%</span>
          <div class="task-progress-container">
            <div class="task-progress-bar" data-progress="${pct}"></div>
          </div>
        </div>
      </div>

      <div class="task-cell file-cell"></div>
      <div class="task-cell updated-at-cell"></div>
    </div>
  `;
  }

  function findSubTaskContainer(taskRow) {
    let el = taskRow.nextElementSibling;
    while (el && !(el.classList && el.classList.contains('sub-task-container'))) {
      el = el.nextElementSibling;
    }
    return el || null;
  }

  function ensureSubTaskContainer(taskRow) {
    const exist = findSubTaskContainer(taskRow);
    if (exist) return exist;

    const wrapper = document.createElement('div');
    wrapper.className = 'sub-task-container hidden';
    wrapper.innerHTML = `
      <div class="task-list-header sub-task-header">
        <div class="task-cell task-actions-cell"></div>
        <div class="task-cell task-title-cell">하위 태스크</div>
        <div class="task-cell assignee-cell">담당자</div>
        <div class="task-cell started-at-cell">시작일</div>
        <div class="task-cell duedate-cell">마감일</div>
        <div class="task-cell status-cell">상태</div>
        <div class="task-cell priority-cell">우선순위</div>
        <div class="task-cell progress-cell">진행률</div>
        <div class="task-cell file-cell">파일</div>
        <div class="task-cell updated-at-cell">최근 수정일</div>
      </div>
      <div class="task-list-body sub-task-body"></div>
    `;
    taskRow.insertAdjacentElement('afterend', wrapper);

    // 상위 행에 토글 아이콘 보장
    const iconWrapper = taskRow.querySelector('.icon-wrapper');
    if (iconWrapper && !iconWrapper.querySelector('.js-toggle-subtasks')) {
      const checkbox = iconWrapper.querySelector('.form-check-input');
      const span = document.createElement('span');
      span.className = 'toggle-icon js-toggle-subtasks';
      span.innerHTML = '<i class="fa-solid fa-caret-right"></i>';
      if (checkbox) checkbox.replaceWith(span);
      else iconWrapper.prepend(span);
    }
    return wrapper;
  }

  async function loadChildrenOnce(container, parentId) {
    if (container.dataset.loaded) return;
    if (container.__busy) return;
    container.__busy = true;
    try {
      const res = await fetch(API.children(parentId));
      if (!res.ok) throw new Error('하위 태스크 로드 실패');
      const list = await res.json();
      const body = container.querySelector('.sub-task-body') || container;
      body.innerHTML = list.map(renderChildRow).join('');
      container.dataset.loaded = '1';
      updateAllProgressBars();
    } finally {
      container.__busy = false;
    }
  }

  // ---------- 폼 ----------
  function showTaskForm(targetContainer, parentTaskId = null) {
    if (currentOpenForm) {
      currentOpenForm.remove();
      currentOpenForm = null;
    }

    const formTemplate = document.getElementById('task-form-template');
    if (!formTemplate) {
      console.warn('태스크 폼 템플릿이 없습니다.');
      return;
    }

    const formClone = formTemplate.cloneNode(true);
    formClone.removeAttribute('id');
    formClone.classList.remove('hidden');
    if (parentTaskId) formClone.dataset.parentId = parentTaskId;

    targetContainer.insertAdjacentElement('beforeend', formClone);
    currentOpenForm = formClone;

    formClone.querySelector('input[name="title"]')?.focus();
    formClone.querySelector('.js-save-task')?.addEventListener('click', handleSaveTask);
    formClone.querySelector('.js-cancel-task')?.addEventListener('click', () => {
      if (!currentOpenForm) return;
      const container = currentOpenForm.closest('.sub-task-container');
      currentOpenForm.remove();
      currentOpenForm = null;

      if (container) {
        const hasExistingSubTasks = container.querySelector('.sub-task-body .task-row');
        if (!hasExistingSubTasks) {
          const parentRow = container.previousElementSibling;
          container.remove();
          if (parentRow?.classList.contains('task-row')) {
            const toggleIcon = parentRow.querySelector('.js-toggle-subtasks');
            if (toggleIcon) {
              const checkbox = document.createElement('input');
              checkbox.type = 'checkbox';
              checkbox.className = 'form-check-input';
              toggleIcon.replaceWith(checkbox);
            }
          }
        }
      }
    });
  }

  async function handleSaveTask(event) {
    const form = event.target.closest('.task-form-row');
    if (!form) return alert('저장 폼을 찾을 수 없습니다.');

    const titleEl = form.querySelector('input[name="title"]');
    const startEl = form.querySelector('input[name="startedAt"]');
    const dueEl = form.querySelector('input[name="dueDate"]');
    const prioEl = form.querySelector('select[name="priority"]');

    const title = titleEl?.value?.trim() || '';
    if (!title) {
      alert('제목을 입력하세요.');
      titleEl?.focus();
      return;
    }

    const taskData = {
      title,
      startDate: startEl?.value || null,
      dueDate: dueEl?.value || null,
      priorityId: getPriorityId(prioEl?.value || 'normal'),
      assigneeId: null,
      parentTaskId: form.dataset.parentId ? Number(form.dataset.parentId) : null,
    };

    try {
      const res = await fetch(API.createTask, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(taskData),
      });
      if (!res.ok) {
        let msg = '저장 실패';
        try {
          const j = await res.json();
          if (j?.message) msg += `: ${j.message}`;
        } catch {}
        alert(msg);
        return;
      }
      alert('저장 완료');
      window.location.reload();
    } catch (e) {
      console.error(e);
      alert('저장 중 오류가 발생했습니다.');
    }
  }

  function getPriorityId(val) {
    switch (val) {
      case 'highest':
        return 5;
      case 'high':
        return 4;
      case 'normal':
        return 3;
      case 'low':
        return 2;
      case 'lowest':
        return 1;
      default:
        return 3;
    }
  }

  // ---------- 상태 팝오버 ----------
  const statusPopover = document.getElementById('status-popover');

  function toggleStatusPopover(cell) {
    if (!statusPopover) return;
    if (statusPopover.hidden || activeStatusPopover !== statusPopover) showStatusPopover(cell);
    else hideStatusPopover();
  }
  async function showStatusPopover(cell) {
    if (!statusPopover) return;
    hideStatusPopover();
    activeStatusPopover = statusPopover;
    const currentTaskId = cell.closest('.task-row')?.dataset.taskId;
    if (!currentTaskId) return;
    const rect = cell.getBoundingClientRect();
    statusPopover.style.left = `${rect.left}px`;
    statusPopover.style.top = `${rect.bottom + window.scrollY + 5}px`;
    statusPopover.hidden = false;
    await populateStatusList(currentTaskId, cell);
  }
  function hideStatusPopover() {
    if (!statusPopover) return;
    statusPopover.hidden = true;
    activeStatusPopover = null;
  }
  async function populateStatusList(taskId, cell) {
    const list = statusPopover.querySelector('.status-list');
    list.innerHTML = '<li>불러오는 중...</li>';
    try {
      const statuses = [
        { id: 1, name: 'Assignments', color: '#e3e3e3' },
        { id: 2, name: 'in-progress', color: '#b5e6ff' },
        { id: 3, name: 'Reviewing', color: '#87cbfb' },
        { id: 4, name: 'Completed', color: '#3041ff' },
      ];
      list.innerHTML = '';
      statuses.forEach((s) => {
        const li = document.createElement('li');
        li.className = 'status-list-item';
        li.dataset.statusId = s.id;
        li.dataset.statusName = s.name;
        li.innerHTML = `<span class="status-color-dot" style="background-color:${s.color};"></span><span>${s.name}</span>`;
        list.appendChild(li);
      });
      statusPopover.querySelector('.status-list').onclick = (e) => {
        const item = e.target.closest('.status-list-item');
        if (!item) return;
        const statusName = item.dataset.statusName;
        const statusSpan = cell.querySelector('.status');
        statusSpan.textContent = statusName;
        statusSpan.className = `status ${statusName}`;
        hideStatusPopover();
      };
      statusPopover.querySelector('.status-add-input').onkeydown = async (e) => {
        if (e.key !== 'Enter') return;
        const newStatusName = e.target.value.trim();
        if (!newStatusName) return;
        alert(`'${newStatusName}' 상태가 추가되었습니다.`);
        e.target.value = '';
        await populateStatusList(taskId, cell);
      };
    } catch {
      list.innerHTML = '<li>목록을 불러오지 못했습니다.</li>';
    }
  }

  // ---------- 클릭(단일) ----------
  document.body.addEventListener('click', function (e) {
    // 더블클릭 직후 발생하는 click 무시
    if (e.__assigneeHandled || e.target.closest('.assignee-cell, .assignee-panel')) {
      e.stopImmediatePropagation();
      e.stopPropagation();
      return;
    }
    // 상단 버튼
    const addBtn = e.target.closest('#task-add-btn');
    if (addBtn) {
      const activeTaskListBody = document.querySelector('.active-task-container .task-list-body');
      if (activeTaskListBody) showTaskForm(activeTaskListBody, null);
      return;
    }
    const deleteBtn = e.target.closest('#task-delete-btn');
    if (deleteBtn) {
      handleDeleteTask();
      return;
    }

    // 상태 팝오버
    const statusCell = e.target.closest('.task-row .task-cell.status-cell');
    if (statusCell) {
      toggleStatusPopover(statusCell);
      return;
    }
    if (activeStatusPopover && !activeStatusPopover.contains(e.target)) hideStatusPopover();

    // 토글 아이콘 클릭만 처리
    const toggleIcon = e.target.closest('.js-toggle-subtasks');
    if (toggleIcon) {
      e.stopPropagation();
      const row = toggleIcon.closest('.task-row');
      if (!row) return;
      const container = findSubTaskContainer(row) || ensureSubTaskContainer(row);
      if (container.__busy) return;

      if (isHidden(container)) {
        (async () => {
          try {
            await loadChildrenOnce(container, row.dataset.taskId);
            showEl(container);
            toggleIcon.classList.add('open');
          } catch (err) {
            console.error(err);
            hideEl(container);
            toggleIcon.classList.remove('open');
            alert(err.message || '하위 태스크를 불러오지 못했습니다.');
          }
        })();
      } else {
        hideEl(container);
        toggleIcon.classList.remove('open');
      }
      return;
    }
  });

  // ---------- 더블클릭(행) ----------
  document.body.addEventListener('dblclick', function (e) {
    // 더블클릭 완료 후 X ms 동안 click 무시
    suppressClickUntil = performance.now() + 350;

    // 토글 아이콘 자체에서의 더블클릭은 무시 (토글은 단일클릭 전용)
    if (e.target.closest('.js-toggle-subtasks')) return;

    const row = e.target.closest('.task-row:not(.task-form-row)');
    if (!row) return;

    const container = ensureSubTaskContainer(row);
    if (container.__busy) return;

    (async () => {
      try {
        await loadChildrenOnce(container, row.dataset.taskId);
        showEl(container);
        row.querySelector('.js-toggle-subtasks')?.classList.add('open');
        const subBody = container.querySelector('.sub-task-body') || container;
        showTaskForm(subBody, row.dataset.taskId);
      } catch (err) {
        console.error(err);
        hideEl(container);
        row.querySelector('.js-toggle-subtasks')?.classList.remove('open');
        alert(err.message || '하위 태스크를 불러오지 못했습니다.');
      }
    })();
  });

  // ---------- 체크박스 ----------
  document.body.addEventListener('change', function (e) {
    if (e.target.matches('.task-row input[type="checkbox"]')) {
      const row = e.target.closest('.task-row');
      row?.classList.toggle('is-selected', e.target.checked);
      syncDeleteButtonState();
    }
  });

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') hideStatusPopover();
  });

  async function handleDeleteTask() {
    const selectedIds = [...document.querySelectorAll('.task-row input[type="checkbox"]:checked')].map((cb) => cb.closest('.task-row')?.dataset.taskId).filter(Boolean);

    if (selectedIds.length === 0) return alert('삭제할 태스크를 선택하세요.');
    if (!confirm(`${selectedIds.length}개의 태스크를 삭제할까요?`)) return;

    try {
      const resp = await fetch(API.bulkDelete, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(selectedIds),
      });
      if (!resp.ok) throw new Error('삭제에 실패했습니다.');

      selectedIds.forEach((id) => {
        const row = document.querySelector(`.task-row[data-task-id="${id}"]`);
        if (row) {
          const next = row.nextElementSibling;
          if (next?.classList.contains('sub-task-container')) next.remove();
          row.remove();
        }
      });
      syncDeleteButtonState();
    } catch (err) {
      console.error(err);
      alert(err.message || '삭제 중 오류가 발생했습니다.');
    }
  }

  function syncDeleteButtonState() {
    const anyChecked = document.querySelector('.task-row input[type="checkbox"]:checked') !== null;
    const delBtn = document.getElementById('task-delete-btn');
    if (delBtn) delBtn.disabled = !anyChecked;
  }

  // ---------- 초기화 ----------
  syncDeleteButtonState();
  updateAllProgressBars();
  const observer = new MutationObserver(updateAllProgressBars);
  observer.observe(document.querySelector('.main-wrapper-body') || document.body, { childList: true, subtree: true });
});
