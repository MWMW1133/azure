// /js/todo.js
// 오늘의 To-do 드롭다운 렌더/토글 전담 스크립트
(() => {
  if (window.__AZURA_TODO_BOUND__) return; // 중복 바인딩 방지(탑바가 여러 번 include 되는 경우)
  window.__AZURA_TODO_BOUND__ = true;

  const q = (sel, root = document) => root.querySelector(sel);

  // 서버 응답을 안전하게 정규화 (snake/camel 혼용 대비)
  const normalize = (raw) => {
    const id = raw?.id ?? raw?.personal_calendar_id ?? raw?.calendarId ?? raw?.calendar_id;
    const title = raw?.title ?? raw?.subject ?? raw?.name ?? "";
    const memo = raw?.memo ?? raw?.description ?? raw?.note ?? "";
    const start = raw?.start ?? raw?.start_at ?? raw?.startAt ?? raw?.begin_at ?? null;
    const end   = raw?.end   ?? raw?.end_at   ?? raw?.endAt   ?? raw?.finish_at ?? null;
    const allDay = raw?.allDay ?? raw?.all_day ?? false;
    const isDone = Boolean(raw?.isDone ?? raw?.is_done ?? raw?.done ?? raw?.completed ?? false);
    return { id, title, memo, start, end, allDay, isDone };
  };

  const fmtTime = (start, end, allDay) => {
    if (allDay) return "종일";
    if (!start || !end) return "";
    const s = new Date(start).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    const e = new Date(end).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    return `${s} - ${e}`;
  };

  const render = (listEl, items = []) => {
    listEl.innerHTML = items.map(r => {
      const it = normalize(r);
      return `
        <tr data-id="${it.id}">
          <td>
            <input type="checkbox" class="form-check-input azura-todo-toggle" ${it.isDone ? "checked" : ""}>
          </td>
          <td class="${it.isDone ? 'text-decoration-line-through text-muted' : ''}">
            ${it.title}
          </td>
          <td>${fmtTime(it.start, it.end, it.allDay)}</td>
          <td>
            <span class="badge ${it.isDone ? 'bg-success' : 'bg-danger'}">
              ${it.isDone ? '완료' : '미완료'}
            </span>
          </td>
          <td>${it.memo}</td>
        </tr>
      `;
    }).join("");
  };

  const fetchTodos = async (listEl) => {
    try {
      const base = window.APP_CTX || '';
      const res = await fetch(`${base}/api/calendar/today`, { credentials: "same-origin" });
      if (!res.ok) throw new Error("오늘 일정 조회 실패");
      const data = await res.json();
      render(listEl, Array.isArray(data) ? data : []);
    } catch (e) {
      console.error(e);
      listEl.innerHTML = `<tr><td colspan="5" class="text-muted">오늘 일정이 없거나 불러오기에 실패했습니다.</td></tr>`;
    }
  };

  const onToggle = async (e) => {
    if (!e.target.classList.contains("azura-todo-toggle")) return;
    const tr = e.target.closest("tr");
    const id = tr?.dataset?.id;
    const done = e.target.checked;

    try {
      const base = window.APP_CTX || '';
      const res = await fetch(`${base}/api/calendar/${id}/toggle-done?done=${done}`, {
        method: "POST",
        credentials: "same-origin"
      });
      if (!res.ok) throw new Error("완료상태 변경 실패");

      // 성공 시 UI 즉시 갱신
      const badge = tr.querySelector(".badge");
      badge.className = `badge ${done ? 'bg-success' : 'bg-danger'}`;
      badge.textContent = done ? "완료" : "미완료";

      const titleTd = tr.children[1];
      titleTd.classList.toggle("text-decoration-line-through", done);
      titleTd.classList.toggle("text-muted", done);
    } catch (err) {
      console.error(err);
      // 실패하면 체크 원복
      e.target.checked = !done;
    }
  };

  const init = () => {
    const listEl = q("#todoList");
    if (!listEl) return;                    // 탑바 To-do 표가 없는 페이지는 스킵
    if (listEl.__AZURA_TODO_WIRED__) return; // 같은 노드에 두 번 바인딩하지 않도록
    listEl.__AZURA_TODO_WIRED__ = true;

    listEl.addEventListener("change", onToggle);
    fetchTodos(listEl);
  };

  // DOMContentLoaded 이후에만 초기화
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
