(function initAssigneePanels() {
  // API 호출 래퍼 (기존 코드에 이미 있다면 이 부분은 제외)
  const api = {
    async searchUsers(q = '') {
      // 이 URL은 실제 API 경로에 맞게 수정해주세요.
      const r = await fetch(`/api/users?query=${encodeURIComponent(q)}`);
      if (!r.ok) throw new Error(`searchUsers ${r.status}`);
      return r.json();
    },
    async assignTask(taskId, userId) {
      // 이 URL은 실제 API 경로에 맞게 수정해주세요.
      const r = await fetch(`/api/tasks/${taskId}/assignee`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId }),
      });
      if (!r.ok) throw new Error(`assign failed ${r.status}`);
      return true;
    },
  };

  let activePanel = null; // 현재 열려있는 패널을 추적하기 위한 변수
  let debounceTimer;

  document.addEventListener('click', (e) => {
    const cell = e.target.closest('.task-cell.assignee-cell');

    // 담당자 셸 클릭시
    if (cell) {
      const panel = cell.querySelector('.assignee-panel');

      if (activePanel && activePanel !== panel) {
        closePanel(activePanel);
      }

      // 창 열기
      if (panel.hidden) {
        openPanel(panel);
      } else {
        closePanel(panel);
      }
      return; // 중복 실행 방지
    }

    // 외부 클릭시 창 닫기
    if (activePanel && !activePanel.contains(e.target)) {
      closePanel(activePanel);
    }
  });

  // ESC 키로 닫기
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && activePanel) {
      closePanel(activePanel);
    }
  });

  // ===== 패널 여는 함수 =====
  function openPanel(panel) {
    panel.hidden = false;
    panel.classList.add('is-open');
    activePanel = panel; // 현재 열린 패널로 지정
    initializePanelContent(panel); // 패널 내용 초기화
  }

  // ===== 패널 닫는 함수 =====
  function closePanel(panel) {
    panel.hidden = true;
    panel.classList.remove('is-open');
    activePanel = null; // 열린 패널 없음으로 지정
  }

  // ===== 패널 내용 초기화 및 이벤트 바인딩 함수 =====
  function initializePanelContent(panel) {
    const cell = panel.closest('.assignee-cell');
    const row = panel.closest('.task-row');
    const taskId = row.dataset.taskId;
    const list = panel.querySelector('.assignee-list');
    const searchInput = panel.querySelector('.assignee-search-input');
    const submitBtn = panel.querySelector('.assignee-submit');
    let selectedId = null;

    // 사용자 목록 로드
    async function loadList(q = '') {
      const users = await api.searchUsers(q);
      list.innerHTML = '';
      users.forEach((u) => {
        const li = document.createElement('li');
        li.className = 'assignee-item';
        li.dataset.id = u.id;
        li.innerHTML = `
                  <div class="assignee-avatar">
                    ${u.avatarUrl ? `<img src="${u.avatarUrl}" alt="">` : (u.name || 'U')[0]}
                  </div>
                  <div class="assignee-meta">
                    <div class="assignee-name">${u.name}</div>
                    <div class="assignee-email">${u.email}</div>
                  </div>
                  <button class="assignee-action" title="배정">+</button>`;
        list.appendChild(li);
      });
    }

    // 검색 입력
    searchInput.value = '';
    searchInput.focus();
    searchInput.oninput = (e) => {
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => loadList(e.target.value.trim()), 200);
    };

    // 목록에서 사용자 선택
    list.onclick = (e) => {
      const item = e.target.closest('.assignee-item');
      if (!item) return;

      selectedId = item.dataset.id;

      list.querySelectorAll('.assignee-item').forEach((li) => li.classList.remove('is-selected'));
      item.classList.add('is-selected');

      submitBtn.disabled = false;
    };

    // 배정하기 버튼 클릭
    submitBtn.onclick = async () => {
      if (!selectedId) return;
      try {
        await api.assignTask(taskId, selectedId);
        closePanel(panel);

        // TODO: 담당자 UI 즉시 업데이트 (예: window.location.reload(); 또는 동적 업데이트)
        window.location.reload();
      } catch (err) {
        console.warn('assign failed', err);
        alert('담당자 배정에 실패했습니다.');
      }
    };

    // 초기 목록 로드
    loadList('');
  }
})();
