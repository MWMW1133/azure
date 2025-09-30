document.addEventListener("DOMContentLoaded", function () {
  let durationPicker = null;

  /* ---------- flatpickr ---------- */
  function ensurePicker() {
    const el = document.getElementById("pplan-duration");
    if (!durationPicker && window.flatpickr && el) {
      durationPicker = flatpickr(el, {
        mode: "range",
        dateFormat: "Y-m-d",
        locale: { rangeSeparator: " ~ " },
      });
    }
  }

  /* ---------- Form modal ---------- */
  function openPopup() {
    const modal = document.getElementById("pplan-modal-overlay");
    const form = document.getElementById("project-pplan-form");
    if (form) form.reset();
    ensurePicker();
    if (durationPicker) durationPicker.clear();
    if (modal) modal.style.display = "flex";
  }
  function closePopup() {
    const modal = document.getElementById("pplan-modal-overlay");
    if (modal) modal.style.display = "none";
  }

  /* ---------- Viewer modal ---------- */
  function openViewer(data) {
    // 채우기
    const $ = (id) => document.getElementById(id);
    if ($("pv-title")) $("pv-title").textContent = data.title || "-";
    if ($("pv-proposer")) $("pv-proposer").textContent = data.proposer || "-";
    if ($("pv-duration")) $("pv-duration").textContent = (data.start || "-") + " ~ " + (data.end || "-");
    if ($("pv-created")) $("pv-created").textContent = data.createdAt || "-";
    if ($("pv-description")) $("pv-description").textContent = data.description || "-";

    // 상태 배지 갱신
    const badge = $("pv-status-badge");
    if (badge) {
      badge.textContent = data.status === "approved" ? "승인됨" : data.status === "rejected" ? "거부됨" : "검토 전";
      badge.className = "pplan-status " + (data.status || "");
    }

    // 열기
    const overlay = document.getElementById("pplan-viewer-overlay");
    if (overlay) overlay.style.display = "flex";
  }
  function closeViewer() {
    const overlay = document.getElementById("pplan-viewer-overlay");
    if (overlay) overlay.style.display = "none";
  }

  /* ---------- Delegated click (한 곳에서 처리) ---------- */
  document.addEventListener("click", (e) => {
    // 1) 계획 추가 버튼
    if (e.target.closest("#btn-add")) {
      e.preventDefault();
      openPopup();
      return;
    }
    // 2) 폼 저장
    if (e.target.closest("#btn-save")) {
      e.preventDefault();
      ensurePicker();
      const selected = durationPicker ? durationPicker.selectedDates : [];
      const projectPlanData = {
        id: (document.getElementById("pplan-id") || {}).value || null,
        title: (document.getElementById("pplan-title") || {}).value || "",
        startDate: selected[0] ? selected[0].toISOString().split("~")[0] : null,
        endDate: selected[1] ? selected[1].toISOString().split("~")[0] : null,
        description: (document.getElementById("pplan-description") || {}).value || "",
      };
      console.log("서버로 전송할 데이터:", projectPlanData);
      /* 실제 저장
      fetch("/api/project/plan", {
        method:"POST",
        headers:{"Content-Type":"application/json"},
        body: JSON.stringify(projectPlanData)
      }).then(r=>r.ok?r.json():Promise.reject(r))
       .then(()=> closePopup())
       .catch(err=>{ console.error(err); alert("저장 실패"); });
      */
      closePopup();
      return;
    }
    // 3) 폼 닫기
    if (e.target.closest(".pplan-form-container .popup-close") || e.target.id === "pplan-modal-overlay") {
      e.preventDefault();
      closePopup();
      return;
    }
    // 4) 행 클릭 → 뷰어 열기
    const row = e.target.closest(".pplan-row");
    if (row) {
      e.preventDefault();
      const d = row.dataset;
      const data = {
        id: d.id,
        title: d.title,
        proposer: d.proposer,
        createdAt: d.createdAt,
        status: d.status, // 'new' | 'approved' | 'rejected'
        start: d.start,
        end: d.end,
        description: d.description,
      };
      openViewer(data);
      return;
    }
    // 5) 뷰어 닫기 (X 버튼 or 배경)
    if (e.target.closest(".pplan-viewer-header .popup-close") || e.target.id === "pplan-viewer-overlay") {
      e.preventDefault();
      closeViewer();
      return;
    }
  });
});
