document.addEventListener('DOMContentLoaded', function () {
  const api = {};

  // ===== 상태 관리 변수 =====
  let currentOpenForm = null;
  let activeStatusPopover = null;

  document.body.addEventListener('click', function (e) {
    // --- Popover 처리 ---
    const statusCell = e.target.closest('.task-row .task-cell.status-cell');
    console.log(statusCell);
    if (statusCell) {
      toggleStatusPopover(statusCell);
      return; // 상태 Popover를 열었으면 다른 클릭 로직은 무시
    }
    if (activeStatusPopover && !activeStatusPopover.contains(e.target)) {
      hideStatusPopover();
    }

    // 태스크 추가
    const addBtn = e.target.closest('#task-add-btn');
    if (addBtn) {
      const activeTaskListBody = document.querySelector('.active-task-container .task-list-body');
      if (activeTaskListBody) {
        showTaskForm(activeTaskListBody, null);
      }
      return;
    }

    // 태스크 삭제
    const deleteBtn = e.target.closest('#task-delete-btn');
    if (deleteBtn) {
      handleDeleteTask();
      return;
    }

    // --- 하위 태스크 토글 처리 ---
    const toggleTrigger = e.target.closest('.js-toggle-subtasks');
    if (toggleTrigger && !e.target.closest('input[type="checkbox"]')) {
      const parentRow = toggleTrigger.closest('[data-task-id]');
      if (parentRow) {
        const subTaskContainer = findSubTaskContainer(parentRow);
        if (subTaskContainer) {
          subTaskContainer.classList.toggle('hidden');
          parentRow.querySelector('.js-toggle-subtasks')?.classList.toggle('open');
        }
      }
      return;
    }
  });

  // 더블클릭시 하위 태스크 추가
  document.body.addEventListener('dblclick', function (e) {
    const taskRow = e.target.closest('.task-row:not(.task-form-row)');
    if (taskRow) {
      const taskId = taskRow.dataset.taskId;
      if (!taskId) return;

      const container = ensureSubTaskContainer(taskRow);
      container.classList.remove('hidden');
      taskRow.querySelector('.js-toggle-subtasks')?.classList.add('open');
      const subBody = container.querySelector('.sub-task-body') || container;
      showTaskForm(subBody, taskId);
    }
  });

  // ESC 키 이벤트 리스너
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') hideStatusPopover();
  });

  // 체크박스 변경 이벤트 리스너
  document.body.addEventListener('change', function (e) {
    if (e.target.matches('.task-row input[type="checkbox"]')) {
      const row = e.target.closest('.task-row');
      row?.classList.toggle('is-selected', e.target.checked);
      syncDeleteButtonState();
    }
  });

  /* 진행률 바 */
  function updateAllProgressBars() {
    document.querySelectorAll('.task-progress-bar').forEach((bar) => {
      const progress = parseInt(bar.dataset.progress || '0', 10);
      bar.style.width = Math.max(0, Math.min(100, progress)) + '%';
    });
  }

  /*   하위 컨테이너 유틸   */
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
    wrapper.className = 'sub-task-container';
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

    //상위 태스크에 토글 아이콘 추가
    const iconWrapper = taskRow.querySelector('.icon-wrapper');
    if (iconWrapper && !iconWrapper.querySelector('.js-toggle-subtasks')) {
      const checkbox = iconWrapper.querySelector('.form-check-input');
      const span = document.createElement('span');
      span.className = 'toggle-icon js-toggle-subtasks';
      span.innerHTML = '<i class="fa-solid fa-caret-right"></i>';

      if (checkbox) {
        checkbox.replaceWith(span);
      } else {
        iconWrapper.prepend(span);
      }
    }
    return wrapper;
  }

  /* ---------- 태스크 폼(추가/수정) 관련 ---------- */
  function showTaskForm(targetContainer, parentTaskId = null) {
    if (currentOpenForm) {
      currentOpenForm.remove();
      currentOpenForm = null;
    }

    const formTemplate = document.getElementById('task-form-template');
    if (!formTemplate) return console.warn('태스크 폼 템플릿이 없습니다.');

    const formClone = formTemplate.cloneNode(true);
    formClone.removeAttribute('id');
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
    const form = event.target.closest('.task-form');
    if (!form) return;
    const titleInput = form.querySelector('input[name="title"]');
    if (!titleInput || !titleInput.value.trim()) {
      alert('태스크 이름을 입력하세요.');
      titleInput?.focus();
      return;
    }
    const start_date = form.querySelector('input[name="startedAt"]')?.value || '';
    const due_date = form.querySelector('input[name="dueDate"]')?.value || '';
    const priority = form.querySelector('select[name="priority"]')?.value || 'normal';

    const taskData = {
      name: titleInput.value.trim(),
      start_date,
      due_date,
      status: 'Assignments',
      priority,
      parentId: form.dataset.parentId || null,
    };

    ///////////////디버그
    const requestUrl = '/api/tasks';
    const requestBody = JSON.stringify(taskData);
    const requestOptions = {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: requestBody,
    };

    console.groupCollapsed('[저장] 태스크 저장');
    console.table(taskData);
    console.log('Request URL:', requestUrl);
    console.log('Request Options:', requestOptions);
    console.groupEnd();
    /////////디버그

    try {
      const response = await fetch('/api/tasks', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(taskData),
      });
      if (!response.ok) throw new Error('태스크 저장에 실패했습니다.');
      window.location.reload();
    } catch (err) {
      console.error(err);
      alert(err.message || '저장 중 오류가 발생했습니다.');
    }
  }

  /* ---------- 태스크 선택/삭제 관련 ---------- */
  function syncDeleteButtonState() {
    const anyChecked = document.querySelector('.task-row input[type="checkbox"]:checked') !== null;
    const delBtn = document.getElementById('task-delete-btn');
    if (delBtn) delBtn.disabled = !anyChecked;
  }
  async function handleDeleteTask() {
    const selectedIds = [...document.querySelectorAll('.task-row input[type="checkbox"]:checked')].map((cb) => cb.closest('.task-row')?.dataset.taskId).filter(Boolean);

    if (selectedIds.length === 0) return alert('삭제할 태스크를 선택하세요.');
    if (!confirm(`${selectedIds.length}개의 태스크를 삭제할까요?`)) return;

    try {
      const resp = await fetch('/api/tasks/bulk-delete', {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ids: selectedIds }),
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

  /* 상태 */
  const statusPopover = document.getElementById('status-popover');
  function toggleStatusPopover(cell) {
    if (!statusPopover) return;
    if (statusPopover.hidden || activeStatusPopover !== statusPopover) {
      showStatusPopover(cell);
    } else {
      hideStatusPopover();
    }
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
        /* 데모 데이터 */ { id: 1, name: 'Assignments', color: '#e3e3e3' },
        { id: 2, name: 'in-progress', color: '#b5e6ff' },
        { id: 3, name: 'Reviewing', color: '#87cbfb' },
        { id: 4, name: 'Completed', color: '#3041ff' },
      ];
      list.innerHTML = '';
      statuses.forEach((status) => {
        const li = document.createElement('li');
        li.className = 'status-list-item';
        li.dataset.statusId = status.id;
        li.dataset.statusName = status.name;
        li.innerHTML = `<span class="status-color-dot" style="background-color: ${status.color};"></span><span>${status.name}</span>`;
        list.appendChild(li);
      });

      statusPopover.querySelector('.status-list').onclick = async (e) => {
        const item = e.target.closest('.status-list-item');
        if (!item) return;
        const statusName = item.dataset.statusName;
        try {
          const statusSpan = cell.querySelector('.status');
          statusSpan.textContent = statusName;
          statusSpan.className = `status ${statusName}`;
          hideStatusPopover();
        } catch (err) {
          alert('상태 변경에 실패했습니다.');
        }
      };
      statusPopover.querySelector('.status-add-input').onkeydown = async (e) => {
        if (e.key !== 'Enter') return;
        const newStatusName = e.target.value.trim();
        if (!newStatusName) return;
        try {
          alert(`'${newStatusName}' 상태가 추가되었습니다.`);
          e.target.value = '';
          await populateStatusList(taskId, cell);
        } catch (err) {
          alert('새 상태 추가에 실패했습니다.');
        }
      };
    } catch (err) {
      list.innerHTML = '<li>목록을 불러오지 못했습니다.</li>';
    }
  }

  /* ---------- 초기화 ---------- */
  syncDeleteButtonState();
  updateAllProgressBars();
  const observer = new MutationObserver(updateAllProgressBars);
  observer.observe(document.querySelector('.main-wrapper-body') || document.body, {
    childList: true,
    subtree: true,
  });
});
