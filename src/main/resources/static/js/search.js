
    // 모달 열릴 때 입력창 자동 포커스
    document.getElementById('globalSearchModal')
        .addEventListener('shown.bs.modal', function () {
        document.getElementById('searchInput').focus();
    });

    // 입력 지우기
    document.getElementById('searchClear').addEventListener('click', function () {
        const input = document.getElementById('searchInput');
        input.value = '';
        input.focus();
    });

    // 엔터 → 검색 실행
    document.getElementById('searchInput').addEventListener('keydown', function (e) {
        if (e.key === 'Enter') runSearch();
    });

    // 버튼 → 검색 실행
    document.getElementById('searchRun').addEventListener('click', runSearch);

    // 더미 검색 로직 (UI 확인용)
    function runSearch() {
        const q = document.getElementById('searchInput').value.trim();
        const activeTab = document.querySelector('#searchTabs .nav-link.active')
            ?.getAttribute("data-bs-target")
            ?.replace("#pane-", ""); // 예: "tasks"

        // 결과 아이템 빌더 (UI용)
        const makeItem = (title, meta, date) => `
            <li class="list-group-item d-flex justify-content-between align-items-center">
                <div>
                    <div class="fw-semibold">${highlight(title, q)}</div>
                    <div class="text-muted">${meta}</div>
                </div>
                <div class="text-muted">${date}</div>
            </li>`;

        renderResults(activeTab || "all", q, data);
    }



    // 키워드 하이라이트 (간단)
    function highlight(text, q) {
        if (!q) return text;
        const esc = q.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        return text.replace(new RegExp(esc, 'gi'), m => `<mark>${m}</mark>`);
    }

    // 선택된 정렬 옵션 가져오기
    const sortOption = document.getElementById("sortSelect").value;
    // 기본값: "deadline"


    //=================================================
    // 더미 데이터
    const data = {
        projects: [{id:1, name:"캘린더 프로젝트", description:"팀 일정 관리"}],
        tasks: [{id:1, title:"UI 개선"}],
        files: [{id:1, file_name:"회의록.docx"}],
        documents: [{id:1, title:"API 명세서"}],
        users: [{id:1, name:"박소현"}]
    };

    console.log(filterByTabAndKeyword("tasks", "UI", data));
    // → [{id:1, title:"UI 개선"}]

    console.log(filterByTabAndKeyword("all", "프로젝트", data));
    // → { projects:[...], tasks:[], files:[], documents:[], users:[] }


    //==================================================
    // 1차 카테고리 필터링
    function filterByTabAndKeyword(tab, keyword, data) {
        const q = keyword.trim().toLowerCase();

        switch (tab) {
            case "projects":
                return data.projects.filter(p =>
                    p.name.toLowerCase().includes(q) ||
                    (p.description && p.description.toLowerCase().includes(q))
                );

            case "tasks":
                return data.tasks.filter(t =>
                    t.title.toLowerCase().includes(q)
                );

            case "files":
                return data.files.filter(f =>
                    f.file_name.toLowerCase().includes(q)
                );

            case "members":
                return data.users.filter(u =>
                    u.name.toLowerCase().includes(q)
                );

            case "all":
            default:
                return {
                    projects: data.projects.filter(p => p.name.toLowerCase().includes(q)),
                    tasks: data.tasks.filter(t => t.title.toLowerCase().includes(q)),
                    files: data.files.filter(f => f.file_name.toLowerCase().includes(q)),
                    users: data.users.filter(u => u.name.toLowerCase().includes(q))
                };
        }
    }


    // 1차 필터링 결과 jsp id대로 배치
    function renderResults(tab, keyword, data) {
        const results = filterByTabAndKeyword(tab, keyword, data);

        // tab 값이 "projects" / "tasks" / "files" / "documents" / "members" 이런 식이므로
        const mapTabToId = {
            projects: "result-project",
            tasks: "result-task",
            files: "result-file",
            members: "result-member"
        };

        const mapTabToField = {
            projects: "name",
            tasks: "title",
            files: "file_name",
            members: "name"
        };

        if (tab === "all") {
            const allResults = []
                .concat(results.projects.map(p => ({ text: p.name, meta: "프로젝트", date: "" })))
                .concat(results.tasks.map(t => ({ text: t.title, meta: "태스크", date: "" })))
                .concat(results.files.map(f => ({ text: f.file_name, meta: "파일", date: "" })))
                .concat(results.users.map(u => ({ text: u.name, meta: "구성원", date: "" })));

            renderList("result-all", allResults, "text", "meta", "date");

            // 동시에 개별 탭도 업데이트
            renderList(mapTabToId.projects, results.projects, mapTabToField.projects);
            renderList(mapTabToId.tasks, results.tasks, mapTabToField.tasks);
            renderList(mapTabToId.files, results.files, mapTabToField.files);
            renderList(mapTabToId.members, results.users, mapTabToField.members);
        } else {
            renderList(mapTabToId[tab], results, mapTabToField[tab]);
        }
    }

    function renderList(containerId, items, field, metaField, dateField) {
        const ul = document.getElementById(containerId);
        if (!ul) return;
        if (!Array.isArray(items)) return;

        if (items.length === 0) {
            ul.innerHTML = `
      <li class="list-group-item text-muted text-center">
        검색 결과가 없습니다.
      </li>
    `;
            return;
        }

        ul.innerHTML = items.map(item => `
    <li class="list-group-item d-flex justify-content-between align-items-center">
      <div>
        <div class="fw-semibold">${item[field]}</div>
        <div class="text-muted">${metaField ? item[metaField] : ""}</div>
      </div>
      <div class="text-muted">${dateField ? item[dateField] : ""}</div>
    </li>
  `).join("");
    }


