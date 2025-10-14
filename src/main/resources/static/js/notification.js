
/** ======================================
 *  [1] 전역 알림 데이터 (더미 or 서버 연동)
 * ====================================== */
 window.notifications = [];


/** ======================================
 *  [2] 알림 렌더링 함수
 * ====================================== */
function renderNotifs(data) {
    const notifList = document.getElementById("notifList");
    if (!notifList) return;
    notifList.innerHTML = "";
    data.forEach(n => {
        let actionsHtml = "";
        if (n.type === "INVITE_ORGANIZATION") {
            actionsHtml = `
              <div class="mt-2 d-flex gap-2">
                <button class="btn btn-sm btn-primary" onclick="handleInviteAction(${n.id}, 'accept')">수락</button>
                <button class="btn btn-sm btn-outline-secondary" onclick="handleInviteAction(${n.id}, 'reject')">거절</button>
              </div>`;
        }
        notifList.innerHTML += `
          <div class="notif-card ${n.isRead ? 'opacity-75' : ''}" data-id="${n.id}">
            <div class="fw-bold">${n.title}</div>
            <div>${n.message}</div>
            ${actionsHtml}
            <div class="text-end text-muted small mt-1">${n.createdAt}</div>
          </div>`;
    });
}
renderNotifs(window.notifications || []);

/** ======================================
 *  [3] 알림 클릭 시 읽음 처리 (전역 이벤트 위임)
 * ====================================== */
document.addEventListener("click", (e) => {
    const notifCard = e.target.closest(".notif-card");
    if (!notifCard) return;
    const id = parseInt(notifCard.dataset.id, 10);
    const notif = window.notifications.find(n => n.id === id);
    if (notif && !notif.isRead) {
        notif.isRead = true;
        renderNotifs(window.notifications);
    }
});


/** ======================================
 *  [4] WebSocket 연결
 * ====================================== */
window.connectNotificationSocket = function (userId) {
    const socket = new SockJS(`${window.APP_CTX}/ws`);
    const client = new StompJs.Client({
        webSocketFactory: () => socket,
        debug: (str) => console.log(str),
        reconnectDelay: 5000,
    });

    client.onConnect = () => {
        console.log("Notification WebSocket connected");
        client.subscribe(`/topic/notifications/${userId}`, (msg) => {
            const data = JSON.parse(msg.body);
            const payload = data.payload;
            const newNotif = {
                id: data.id || Date.now(),
                type: data.type,
                title:
                    data.type === "INVITE_ORGANIZATION"
                        ? "조직 초대 알림"
                        : "새 알림",
                message:
                    data.type === "INVITE_ORGANIZATION"
                        ? `${payload.sender}님이 ${payload.organization} 조직에 초대했습니다.`
                        : payload.message || "새로운 알림이 있습니다.",
                organizationId: payload.organizationId,
                link: payload.link || "#",
                createdAt: new Date().toLocaleString(),
                isRead: false
            };
            window.notifications.unshift(newNotif);
            renderNotifs(window.notifications);

            // 시각 효과 (벨 색상 + 첫 카드 하이라이트)
            const bellIcon = document.querySelector(".bi-bell");
            if (bellIcon) {
                bellIcon.classList.add("text-danger");
                setTimeout(() => bellIcon.classList.remove("text-danger"), 2000);
            }

            const firstCard = document.querySelector(".notif-card");
            if (firstCard) {
                firstCard.style.backgroundColor = "#eaf1ff";
                setTimeout(() => (firstCard.style.backgroundColor = "#f9f9f9"), 2000);
            }

            // notifList가 없는 경우 (탑바 없는 페이지)
            const notifList = document.getElementById("notifList");
            if (!notifList) {
                alert("새로운 알림이 도착했습니다!");
            }
        });
    };
    client.activate();
};

/** ======================================
 *  [5] 초대 수락/거절 처리
 * ====================================== */
window.handleInviteAction = function (notifId, action) {
    const notif = window.notifications.find(n => n.id === notifId);
    if (!notif) return;
    fetch(`/api/invite/${action}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ organizationId: notif.organizationId })
    })
        .then(res => res.json())
        .then(data => {
            alert(data.message);
            window.notifications = window.notifications.filter(n => n.id !== notifId);
            renderNotifs(window.notifications);
        })
        .catch(err => alert("처리 중 오류 발생"));
};

