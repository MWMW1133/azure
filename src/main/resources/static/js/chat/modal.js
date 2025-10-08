

// 만들어야 하는 것
// 메세지 쌓이면 쌓인 알림 갯수 사이드바 리스트 옆에 숫자 알림 (선택이긴 함)


function openChatModal() {
    document.getElementById('chatModal').classList.add('active');
}
function closeChatModal() {
    document.getElementById('chatModal').classList.remove('active');
}


// 말풍선 시간 처리 (걍 프론트에서 처리)
function formatTime(date = new Date()) {
    return date.toLocaleTimeString('ko-KR', {
        hour: 'numeric',
        minute: 'numeric',
        hour12: true
    });
}

// 사용 예시
const time = formatTime(); // "오전 10:31"

// 채팅방 생성 모달
function openCreateChatModal() {
    document.getElementById('createChatModal').classList.add('active');
}
function closeCreateChatModal() {
    document.getElementById('createChatModal').classList.remove('active');
}

// 사이드바 dm 클릭
function toggleDMList() {
    const dmList = document.getElementById("dmList");
    const dmSearchBox = document.getElementById("dmSearchBox");
    const icon = document.getElementById("dmToggleIcon");

    if (dmList.style.display === "none") {
        dmList.style.display = "block";
        dmSearchBox.style.display = "block";
        icon.classList.remove("bi-caret-right-fill");
        icon.classList.add("bi-caret-down-fill");
    } else {
        dmList.style.display = "none";
        dmSearchBox.style.display = "none";
        icon.classList.remove("bi-caret-down-fill");
        icon.classList.add("bi-caret-right-fill");
    }
}

// DM 검색 필터
function filterDM(query) {
    query = query.toLowerCase();
    const items = document.querySelectorAll("#dmList li");
    items.forEach(li => {
        if (li.textContent.toLowerCase().includes(query)) {
            li.style.display = "block";
        } else {
            li.style.display = "none";
        }
    });
}



// ================================
// 더미 데이터
// ================================
// 나중에 호출할때는 지우거나 꼭 수정하기!!!!!!!!!1 fetch("/api/projects") 이런 식으로
// 그리고 밑에 함수에서도 dummy 쓰는 부분 실제 데이터셋으로 다 바꾸기
const dummyProjects = ["프로젝트 1", "프로젝트 2", "프로젝트 3"];
const dummyDMs = ["홍길동", "김철수", "이영희"];
const dummyMessages = {
    "프로젝트 1": [
        { sender: "홍길동", text: "안녕하세요 👋", time: "오전 10:30", side: "left" },
        { sender: "나", text: "네 반가워요!", time: "오전 10:31", side: "right" }
    ],
    "프로젝트 2": [
        { sender: "김철수", text: "회의 언제하나요?", time: "오후 2:00", side: "left" }
    ],
    "프로젝트 3": [],
    "홍길동": [
        { sender: "홍길동", text: "DM 테스트", time: "오전 9:10", side: "left" }
    ],
    "김철수": [],
    "이영희": []
};


// 그룹 채팅 목록 그리기
function renderGroupChats() {
    const list = document.getElementById("groupChatList");
    list.innerHTML = "";
    dummyProjects.forEach(name => {
        const li = document.createElement("li");
        // li.textContent = name;
        // list.appendChild(li);
        li.textContent = name;
        li.dataset.room = name;
        li.addEventListener("click", () => selectRoom(li, name));
        list.appendChild(li);
    });
}

// DM 목록 그리기
function renderDMs() {
    const list = document.getElementById("dmList");
    list.innerHTML = "";
    dummyDMs.forEach(name => {
        const li = document.createElement("li");
        // li.textContent = name;
        // list.appendChild(li);
        li.textContent = name;
        li.dataset.room = name;
        li.addEventListener("click", () => selectRoom(li, name));
        list.appendChild(li);

    });
}


// ================================
// 메시지 렌더링
// ================================
function renderMessages(roomName) {
    const container = document.getElementById("chatMessages");
    container.innerHTML = ""; // 초기화
    const messages = dummyMessages[roomName] || [];

    messages.forEach(msg => {
        const div = document.createElement("div");
        div.className = `message ${msg.side}`;
        div.innerHTML = `
            ${msg.side === "left" ? `<img src="/images/profile1.png" class="avatar">` : ""}
            <div class="bubble">
                <div class="meta">
                    <span class="sender">${msg.sender}</span>
                    <span class="time">${msg.time}</span>
                </div>
                <p>${msg.text}</p>
            </div>
            ${msg.side === "right" ? `<img src="/images/my-cat.png" class="avatar">` : ""}
        `;
        container.appendChild(div);
    });
}


// 공통 바인딩 (그룹/DM 클릭 시 헤더 반영 + active 관리)
function selectRoom(clickedLi, roomName) {
    // 모든 active 제거
    document.querySelectorAll("#groupChatList li, #dmList li")
        .forEach(li => li.classList.remove("active"));

    // 현재 클릭한 방 active 추가
    clickedLi.classList.add("active");

    // 헤더 갱신
    document.getElementById("chatHeaderTitle").textContent = roomName;

    // 메시지 갱신
    renderMessages(roomName);
}

// 그룹 채팅방 생성 클릭시 나오는 모달에 나오는 리스트
// 채팅방 생성 모달 select 채우기
function renderProjectSelect() {
    const select = document.getElementById("projectSelect");
    if (!select) return;
    select.innerHTML = "";
    dummyProjects.forEach(name => {
        const opt = document.createElement("option");
        opt.textContent = name;
        select.appendChild(opt);
    });
}




// ================================
// 초기화 (페이지 새로고침)
// ================================
document.addEventListener("DOMContentLoaded", () => {
    renderGroupChats();
    renderDMs();
    renderProjectSelect();

    // 기본 첫 방 로딩 (예: 프로젝트 1)
    if (dummyProjects.length > 0) {
        document.getElementById("chatHeaderTitle").textContent = dummyProjects[0];
        renderMessages(dummyProjects[0]);
        document.querySelector("#groupChatList li").classList.add("active");
    }
});



