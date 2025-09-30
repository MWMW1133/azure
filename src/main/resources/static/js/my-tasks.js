document.addEventListener("DOMContentLoaded", function () {
  // 진행률 바 업데이트 함수
  function updateAllProgressBars() {
    const allProgressBars = document.querySelectorAll(".task-progress-bar");
    allProgressBars.forEach(function (bar) {
      const progress = parseInt(bar.dataset.progress || "0", 10);
      bar.style.width = progress + "%";
      console.log("Found bar:", bar, "progress:", progress);
    });
  }

  // =====================
  // 토글 아이콘 로직
  // =====================
  document.body.addEventListener("click", function (event) {
    const toggleIcon = event.target.closest(".js-toggle-subtasks");
    if (toggleIcon) {
      const parentRow = toggleIcon.closest(".task-row");
      const subTaskContainer = parentRow.nextElementSibling;
      if (subTaskContainer && subTaskContainer.classList.contains("sub-task-container")) {
        subTaskContainer.classList.toggle("hidden");
        toggleIcon.classList.toggle("open");
      }
    }
  });

  updateAllProgressBars();

  const observer = new MutationObserver(() => {
    updateAllProgressBars();
  });

  observer.observe(document.body, {
    childList: true,
    subtree: true,
  });
});
