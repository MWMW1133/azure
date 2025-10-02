// project-plan.js (최종 저장 연동 버전 - 수정 완료)
(function () {
  let durationPicker = null;
  let clickHandler = null;

  function showToast(message = '완료되었습니다.', type = 'success', opts = {}) {
    const el = document.getElementById('planToast');
    if (!el) return;
    const container = el.closest('.toast-container');
    const pos = opts.position || 'top-end';
    container.className =
      `toast-container position-fixed p-3 ` +
      `${pos.includes('bottom') ? 'bottom-0' : 'top-0'} ` +
      `${pos.includes('start') ? 'start-0' : 'end-0'}`;

    el.className = 'toast clean-toast';
    el.classList.add(`toast-${type}`);
    el.querySelector('.toast-body').textContent = message;

    const iconEl = el.querySelector('.toast-icon');
    const icons = { success: '✔', error: '✖', warning: '!', info: 'ℹ' };
    if (iconEl) iconEl.textContent = icons[type] ?? 'ℹ';

    const delay = Number(opts.duration || 2200);
    const t = bootstrap.Toast.getOrCreateInstance(el, { autohide: true, delay });
    t.show();
  }

  function ensurePicker(root = document) {
    const el = root.querySelector('#pplan-duration');
    if (durationPicker && durationPicker.input !== el) {
      try { durationPicker.destroy(); } catch (e) {}
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

  function openViewer(data) {
    const $ = (id) => document.getElementById(id);
    $('pv-title') && ($('pv-title').textContent = data.title || '-');
    $('pv-proposer') && ($('pv-proposer').textContent = data.proposer || '-');
    $('pv-duration') && ($('pv-duration').textContent = (data.start || '-') + ' ~ ' + (data.end || '-'));
    $('pv-created') && ($('pv-created').textContent = data.createdAt || '-');
    $('pv-description') && ($('pv-description').textContent = data.description || '-');
    applyStatusToViewer(data.status || 'PENDING');

    const btnA = document.getElementById('btn-approved');
    const btnR = document.getElementById('btn-rejected');
    if (btnA && btnR) {
      btnA.disabled = false;
      btnR.disabled = false;
      if (data.status && data.status !== 'PENDING') {
        btnA.disabled = true;
        btnR.disabled = true;
      }
    }

    const overlay = document.getElementById('pplan-viewer-overlay');
    if (overlay) {
      overlay.style.display = 'flex';
      overlay.dataset.planId = data.id ?? '';
      overlay.dataset.status = data.status || 'PENDING';
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
    badge.textContent =
      status === 'APPROVED' ? '승인됨'
      : status === 'REJECTED' ? '거부됨'
      : '검토 전';
  }

  function applyStatusToRow(planId, status) {
    const esc = (s) => window.CSS && CSS.escape ? CSS.escape(s) : String(s).replace(/"/g, '\\"');
    const row = document.querySelector(`.pplan-row[data-id="${esc(planId)}"]`);
    if (!row) return;
    row.dataset.status = status;

    const pill = row.querySelector('.pplan-status');
    if (pill) {
      pill.className = 'pplan-status ' + status;
      pill.textContent =
        status === 'APPROVED' ? '승인됨'
        : status === 'REJECTED' ? '거부됨'
        : '검토 전';
    }
  }

  function updatePlanStatus(planId, status) {
    const url = `/api/project-plan/${encodeURIComponent(planId)}/status?status=${status}`;
    return fetch(url, { method: 'PUT' })
      .then((res) => {
        if (!res.ok) throw new Error('status update failed');
        return res.json();   // ✅ 서버에서 DTO 반환 필요
      });
  }

  function bindDelegated(rootEl) {
    if (clickHandler) document.removeEventListener('click', clickHandler);

    clickHandler = (e) => {
      if (!rootEl.contains(e.target)) return;

      // 계획 추가 버튼
      if (e.target.closest('#btn-add')) {
        e.preventDefault(); openPopup(); return;
      }

      // 폼 저장
      if (e.target.closest('#btn-save')) {
        e.preventDefault();
        ensurePicker(document);
        const sel = durationPicker ? durationPicker.selectedDates : [];
        const toYMD = (d) => (d ? d.toISOString().slice(0, 10) : null);

        const payload = {
          proposerId: 1, // TODO: 로그인 사용자
          organizationId: 1, // TODO: 세션에서
          name: document.getElementById('pplan-title')?.value || '',
          description: document.getElementById('pplan-description')?.value || '',
          startDate: toYMD(sel[0]),
          dueDate: toYMD(sel[1])
        };

        fetch('/api/project-plan', {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: new URLSearchParams(payload).toString(),
        })
          .then((res) => {
            if (!res.ok) throw new Error('등록 실패');
            return res.json();
          })
          .then((data) => {
            closePopup();
            showToast('계획이 등록되었습니다.', 'success');
            document.dispatchEvent(new CustomEvent('pplan:refresh'));
          })
          .catch(() => showToast('계획 등록 실패', 'error'));
        return;
      }

      // 닫기
      if (e.target.closest('.pplan-form-container .popup-close') || e.target.id === 'pplan-modal-overlay') {
        e.preventDefault(); closePopup(); return;
      }

      // 행 클릭
      const row = e.target.closest('.pplan-row');
      if (row) {
        e.preventDefault();
        const d = row.dataset;
        openViewer({
          id: d.id, title: d.title, proposer: d.proposer,
          createdAt: d.createdAt, status: d.status,
          start: d.start, end: d.end, description: d.description,
        });
        return;
      }

      // 뷰어 닫기
      if (e.target.closest('.pplan-viewer-header .popup-close') || e.target.id === 'pplan-viewer-overlay') {
        e.preventDefault(); closeViewer(); return;
      }

      // 승인 버튼
      if (e.target.closest('#btn-approved')) {
        e.preventDefault();
        const overlay = document.getElementById('pplan-viewer-overlay');
        const planId = overlay?.dataset.planId;
        const status = overlay?.dataset.status || 'PENDING';
        if (!planId) return;
        if (status !== 'PENDING') { showToast('이미 처리됨', 'info'); return; }

        if (!confirm('이 계획을 승인하시겠습니까?')) return;
        updatePlanStatus(planId, 'APPROVED')
          .then((data) => {
            applyStatusToViewer(data.status);
            applyStatusToRow(planId, data.status);
            overlay.dataset.status = data.status;
            closeViewer();
            showToast('프로젝트 승인 완료', 'success');
          })
          .catch(() => showToast('승인 실패', 'error'));
        return;
      }

      // 거절 버튼
      if (e.target.closest('#btn-rejected')) {
        e.preventDefault();
        const overlay = document.getElementById('pplan-viewer-overlay');
        const planId = overlay?.dataset.planId;
        const status = overlay?.dataset.status || 'PENDING';
        if (!planId) return;
        if (status !== 'PENDING') { showToast('이미 처리됨', 'info'); return; }

        if (!confirm('이 계획을 거절하시겠습니까?')) return;
        updatePlanStatus(planId, 'REJECTED')
          .then((data) => {
            applyStatusToViewer(data.status);
            applyStatusToRow(planId, data.status);
            overlay.dataset.status = data.status;
            closeViewer();
            showToast('프로젝트 거절 완료', 'success');
          })
          .catch(() => showToast('거절 실패', 'error'));
        return;
      }
    };

    document.addEventListener('click', clickHandler);
  }

  window.ProjectPlan = {
    mount(rootEl) { ensurePicker(rootEl || document); bindDelegated(rootEl || document); },
    unmount() {
      if (clickHandler) document.removeEventListener('click', clickHandler);
      clickHandler = null;
      if (durationPicker) { try { durationPicker.destroy(); } catch (e) {} durationPicker = null; }
    },
  };
})();
