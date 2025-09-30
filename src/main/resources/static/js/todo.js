document.addEventListener("DOMContentLoaded", () => {
    const todoList = document.getElementById("todoList");

    if (!todoList) {
        console.warn("todoList 요소 없음 → topbar가 로드되지 않은 상태");
        return;
    }

    // 백엔드 api 호출 (주소는 내맘대로함 나중에 수정필요)
    // 프로젝트 태스크 (나의 작업)의 마감일이 당일인 경우? 혹은 해당 기간내에 있을 경우 보여주기
    // const fetchTodos = async () => {
    //     try {
    //         const res = await fetch("/api/calendar/today"); // 오늘 일정 조회
    //         if (!res.ok) throw new Error("불러오기 실패");
    //         const todos = await res.json();
    //         renderTodos(todos);
    //     } catch (err) {
    //         console.error("투두 불러오기 실패:", err);
    //     }
    // };

    // 더미 데이터 (근데 이미 api 데이터 기반으로해서 지금은 안나옴)
    const dummyTodos = [
        { id: 1, title: "회의 자료 정리", time: "10:00 - 12:00", memo: "2시 회의실 302호" },
        { id: 2, title: "레퍼런스 찾기", time: "15:00 - 16:00", memo: "시각 자료 위주" },
        { id: 3, title: "사업계획서 검토", time: "09:20 - 09:50", memo: "피드백 반영" },
        { id: 4, title: "클라이언트 원격회의", time: "16:30 - 17:30", memo: "내용 정리하기" },
    ];

    // 렌더링 함수
    const renderTodos = (todos) => {
        todoList.innerHTML = "";
        todos.forEach(todo => {
            const isDone = savedDone[todo.id] || false;
            todoList.innerHTML += `
              <tr data-id="${todo.id}">
                <td><input type="checkbox" class="form-check-input toggle-check" ${isDone ? "checked" : ""}></td>
                <td class="${isDone ? 'text-decoration-line-through text-muted' : ''}">${todo.title}</td>
                <td>${todo.time}</td>
                <td>
                  <span class="badge ${isDone ? 'bg-success' : 'bg-danger'}">
                    ${isDone ? '완료' : '미완료'}
                  </span>
                </td>
                <td>${todo.memo}</td>
              </tr>
            `;
        });
    };

    //  시간 포맷 유틸 (필요없으면 지워도 상관x)
    const formatTime = (start, end) => {
        if (!start || !end) return "";
        const s = new Date(start).toLocaleTimeString([], {hour: "2-digit", minute: "2-digit"});
        const e = new Date(end).toLocaleTimeString([], {hour: "2-digit", minute: "2-digit"});
        return `${s} - ${e}`;
    };


    //  체크박스 토글
    todoList.addEventListener("change", async (e) => {
        if (!e.target.classList.contains("toggle-check")) return;
        const id = e.target.closest("tr").dataset.id;
        const checked = e.target.checked;

        try {
            // 서버에 토글 요청 (액션 엔드포인트 방식)
            // 마찬가지로 이후 api 주소 수정필요
            // await fetch(`/api/calendar/${id}/toggle-done`, {
            //     method: "POST"
            // });
            // 다시 새로고침해서 UI 반영
            // fetchTodos();
        } catch (err) {
            console.error("완료 상태 변경 실패:", err);
        }
    });

    // 초기 렌더링
    // fetchTodos();
});
