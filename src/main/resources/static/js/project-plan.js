// project-plan.js (정리본)
(function () {
  // ───────────────────────────────────────────────────────────
  // State
  // ───────────────────────────────────────────────────────────
  let durationPicker = null;
  let clickHandler = null;

  // ───────────────────────────────────────────────────────────
  // Utils
  // ───────────────────────────────────────────────────────────
  function showToast(message = '완료되었습니다.', type = 'success', opts = {}) {
    const el = document.getElementById('planToast');
    if (!el) return;

    // 위치 설정
    const container = el.closest('.toast-container');
    const pos = opts.position || 'top-end';
    container.className = `toast-container position-fixed p-3 ` + `${pos.includes('bottom') ? 'bottom-0' : 'top-0'} ` + `${pos.includes('start') ? 'start-0' : 'end-0'}`;

    // 클래스/메시지/아이콘
    el.className = 'toast clean-toast';
    el.classList.add(`toast-${type}`);
    el.querySelector('.toast-body').textContent = message;

    const iconEl = el.querySelector('.toast-icon');
    const icons = { success: '✔', error: '✖', warning: '!', info: 'ℹ' };
    if (iconEl) iconEl.textContent = icons[type] ?? 'ℹ';

    // Bootstrap 토스트
    const delay = Number(opts.duration || 2200);
    const t = bootstrap.Toast.getOrCreateInstance(el, { autohide: true, delay });
    t.show();
  }

  // 날짜 선택기(flatpickr) 보장
  function ensurePicker(root = document) {
    const el = root.querySelector('#pplan-duration');
    if (durationPicker && durationPicker.input !== el) {
      try {
        durationPicker.destroy();
      } catch (e) {}
      durationPicker = null;
    }
    if (!durationPicker && window.flatpickr && el) {
      durationPicker = flatpickr(el, {
        mode: 'range',
        dateFormat: 'Y-m-d',
        locale: { rangeSeparator: ' ~ ' },
      });
    }
  }

  // ───────────────────────────────────────────────────────────
  // Form Modal (등록/수정 폼)
  // ───────────────────────────────────────────────────────────
  function openPopup() {
    const modal = document.getElementById('pplan-modal-overlay');
    const form = document.getElementById('project-pplan-form');
    if (form) form.reset();
    ensurePicker(document);
    if (durationPicker) durationPicker.clear();
    if (modal) modal.style.display = 'flex';
  }
  function closePopup() {
    const modal = document.getElementById('pplan-modal-overlay');
    if (modal) modal.style.display = 'none';
  }

  // ───────────────────────────────────────────────────────────
  // Viewer (읽기 전용 뷰어)
  // ───────────────────────────────────────────────────────────
  function openViewer(data) {
    const $ = (id) => document.getElementById(id);

    // 내용 주입
    $('pv-title') && ($('pv-title').textContent = data.title || '-');
    $('pv-proposer') && ($('pv-proposer').textContent = data.proposer || '-');
    $('pv-duration') && ($('pv-duration').textContent = (data.start || '-') + ' ~ ' + (data.end || '-'));
    $('pv-created') && ($('pv-created').textContent = data.createdAt || '-');
    $('pv-description') && ($('pv-description').textContent = data.description || '-');

    // 상태 뱃지
    applyStatusToViewer(data.status || 'new');

    // 버튼 상태 초기화/잠금
    const btnA = document.getElementById('btn-approved');
    const btnR = document.getElementById('btn-rejected');
    if (btnA && btnR) {
      btnA.disabled = false;
      btnR.disabled = false;
      if (data.status && data.status !== 'new') {
        btnA.disabled = true;
        btnR.disabled = true;
      }
    }

    // 오버레이 오픈 + 메타 저장
    const overlay = document.getElementById('pplan-viewer-overlay');
    if (overlay) {
      overlay.style.display = 'flex';
      overlay.dataset.planId = data.id ?? '';
      overlay.dataset.status = data.status || 'new';
    }
  }

  function closeViewer() {
    const overlay = document.getElementById('pplan-viewer-overlay');
    if (overlay) overlay.style.display = 'none';
  }

  function applyStatusToViewer(status) {
    const badge = document.getElementById('pv-status-badge');
    if (!badge) return;
    badge.className = 'pplan-status ' + (status || '');
    badge.textContent = status === 'approved' ? '승인됨' : status === 'rejected' ? '거부됨' : '검토 전';
  }

  function applyStatusToRow(planId, status) {
    const esc = (s) => (window.CSS && CSS.escape ? CSS.escape(s) : String(s).replace(/"/g, '\\"'));
    const row = document.querySelector(`.pplan-row[data-id="${esc(planId)}"]`);
    if (!row) return;

    row.dataset.status = status;

    const pill = row.querySelector('.pplan-status');
    if (pill) {
      pill.className = 'pplan-status ' + status;
      pill.textContent = status === 'approved' ? '승인됨' : status === 'rejected' ? '거부됨' : '검토 전';
    }
  }

  // ───────────────────────────────────────────────────────────
  // API
  // ───────────────────────────────────────────────────────────
  function updatePlanStatus(planId, status) {
    const url = `/api/project-plan/${encodeURIComponent(planId)}/status`;
    if (typeof fetch !== 'function') {
      return Promise.resolve(); // 서버 연결 전 폴백
    }
    return fetch(url, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status }),
    }).then((res) => {
      if (!res.ok) throw new Error('status update failed');
      try {
        return res.json();
      } catch {
        return;
      }
    });
  }

  // ───────────────────────────────────────────────────────────
  // Event Delegation
  // ───────────────────────────────────────────────────────────
  function bindDelegated(rootEl) {
    // 중복 바인딩 방지
    if (clickHandler) document.removeEventListener('click', clickHandler);

    clickHandler = (e) => {
      if (!rootEl.contains(e.target)) return;

      // 1) 계획 추가 버튼
      if (e.target.closest('#btn-add')) {
        e.preventDefault();
        openPopup();
        return;
      }

      // 2) 폼 저장
      if (e.target.closest('#btn-save')) {
        e.preventDefault();
        ensurePicker(document);
        const sel = durationPicker ? durationPicker.selectedDates : [];
        const toYMD = (d) => (d ? d.toISOString().slice(0, 10) : null);

        const payload = {
          id: document.getElementById('pplan-id')?.value || null,
          title: document.getElementById('pplan-title')?.value || '',
          startDate: toYMD(sel[0]),
          endDate: toYMD(sel[1]),
          description: document.getElementById('pplan-description')?.value || '',
        };

        // TODO: 실제 저장 API 연동
        // fetch('/api/project/plan', { ...payload... })
        //   .then(...)
        closePopup();
        showToast('계획이 등록되었습니다.', 'success', { duration: 2500, position: 'bottom-end' });
        return;
      }

      // 3) 폼 닫기
      if (e.target.closest('.pplan-form-container .popup-close') || e.target.id === 'pplan-modal-overlay') {
        e.preventDefault();
        closePopup();
        return;
      }

      // 4) 행 클릭 → 뷰어 열기
      const row = e.target.closest('.pplan-row');
      if (row) {
        e.preventDefault();
        const d = row.dataset;
        openViewer({
          id: d.id,
          title: d.title,
          proposer: d.proposer,
          createdAt: d.createdAt,
          status: d.status,
          start: d.start,
          end: d.end,
          description: d.description,
        });
        return;
      }

      // 5) 뷰어 닫기
      if (e.target.closest('.pplan-viewer-header .popup-close') || e.target.id === 'pplan-viewer-overlay') {
        e.preventDefault();
        closeViewer();
        return;
      }

      // 6) 승인
      if (e.target.closest('#btn-approved')) {
        e.preventDefault();
        const overlay = document.getElementById('pplan-viewer-overlay');
        const planId = overlay?.dataset.planId;
        const status = overlay?.dataset.status || 'new';
        if (!planId) return;

        if (status !== 'new') {
          // 이미 처리된 건
          showToast('이미 처리된 계획입니다.', 'info', { position: 'bottom-end' });
          return;
        }
        if (!confirm('이 계획을 승인하시겠습니까? 승인 후에는 상태를 변경할 수 없습니다.')) return;

        updatePlanStatus(planId, 'approved')
          .then(() => {
            applyStatusToViewer('approved');
            applyStatusToRow(planId, 'approved');
            // 버튼 잠그기
            const btnA = document.getElementById('btn-approved');
            const btnR = document.getElementById('btn-rejected');
            if (btnA && btnR) {
              btnA.disabled = true;
              btnR.disabled = true;
            }
            // 상태 저장
            overlay.dataset.status = 'approved';

            showToast('프로젝트가 승인되었습니다.', 'success', { position: 'bottom-end' });
            // 닫고 새로고침/재로드가 필요하면 여기서 호출
            closeViewer();
            // window.Router?.go?.('plan'); // 라우터 쓴다면 이런 식으로
            document.dispatchEvent(new CustomEvent('pplan:refresh')); // 필요 시 리스너에서 목록 갱신
          })
          .catch(() => {
            showToast('승인 처리에 실패했습니다.', 'error', { position: 'bottom-end' });
          });
        return;
      }

      // 7) 거절
      if (e.target.closest('#btn-rejected')) {
        e.preventDefault();
        const overlay = document.getElementById('pplan-viewer-overlay');
        const planId = overlay?.dataset.planId;
        const status = overlay?.dataset.status || 'new';
        if (!planId) return;

        if (status !== 'new') {
          showToast('이미 처리된 계획입니다.', 'info', { position: 'bottom-end' });
          return;
        }
        if (!confirm('이 계획을 거절하시겠습니까? 거절 후에는 상태를 변경할 수 없습니다.')) return;

        updatePlanStatus(planId, 'rejected')
          .then(() => {
            applyStatusToViewer('rejected');
            applyStatusToRow(planId, 'rejected');
            const btnA = document.getElementById('btn-approved');
            const btnR = document.getElementById('btn-rejected');
            if (btnA && btnR) {
              btnA.disabled = true;
              btnR.disabled = true;
            }
            overlay.dataset.status = 'rejected';

            showToast('프로젝트가 거절되었습니다.', 'success', { position: 'bottom-end' });
            closeViewer();
            document.dispatchEvent(new CustomEvent('pplan:refresh'));
          })
          .catch(() => {
            showToast('거절 처리에 실패했습니다.', 'error', { position: 'bottom-end' });
          });
        return;
      }
    };

    document.addEventListener('click', clickHandler);
  }

  // ───────────────────────────────────────────────────────────
  // Public API (라우터에서 사용)
  // ───────────────────────────────────────────────────────────
  window.ProjectPlan = {
    mount(rootEl) {
      ensurePicker(rootEl || document);
      bindDelegated(rootEl || document);
    },
    unmount() {
      if (clickHandler) document.removeEventListener('click', clickHandler);
      clickHandler = null;

      if (durationPicker) {
        try {
          durationPicker.destroy();
        } catch (e) {}
        durationPicker = null;
      }
    },
  };
})();
