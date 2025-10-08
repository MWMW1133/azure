// document.addEventListener("DOMContentLoaded", () => {
//     const notifList = document.getElementById("notifList");
//     if (!notifList) {
//         console.warn("notifList 요소가 없습니다. (조직 미소속 사용자)");
//     }
//
//     //  렌더링 함수
//     const renderNotifs = (data) => {
//         notifList.innerHTML = "";
//         data.forEach(n => {
//             notifList.innerHTML += `
//         <a href="${n.link}" class="text-decoration-none text-dark" data-id="${n.id}">
//           <div class="notif-card ${n.isRead ? 'opacity-75' : ''}">
//             <div class="fw-bold">${n.title}</div>
//             <div>${n.message}</div>
//             <div class="text-end text-muted small">${n.createdAt}</div>
//           </div>
//         </a>
//       `;
//         });
//     };
//
//
//     //  알림 클릭 시 읽음 처리
//     notifList.addEventListener("click", (e) => {
//         const notifCard = e.target.closest(".notif-card");
//         if (!notifCard) return;
//
//         const id = parseInt(notifCard.dataset.id, 10);
//         const notif = notifications.find(n => n.id === id);
//
//         if (notif && !notif.isRead) {
//             notif.isRead = true; // UI 반영
//             renderNotifs(notifications);
//
//             // TODO: 서버 연동 (읽음 처리)
//             // 근데 알림은 RESTful보다 액션 엔드포인트가 나을듯.. 주소는 임시
//             // fetch(`/api/notifications/${id}/read`, { method: "POST" })
//             //     .then(res => {
//             //         if (res.ok) console.log("알림 읽음 처리 완료");
//             //         else console.error("읽음 처리 실패", res.status);
//             //     })
//             //     .catch(err => console.error("에러 발생", err));
//         }
//     });
//
//
//     // ==============================
//     //  [WebSocket 실시간 알림 연결]
//     // ==============================
//
//     function connectNotificationSocket(userId) {
//         const socket = new SockJS(`${window.APP_CTX}/ws`);
//         const client = Stomp.over(socket);
//         client.connect({}, () => {
//             console.log("Notification WebSocket connected");
//
//             // 개인별 채널 구독
//             client.subscribe(`/topic/notifications/${userId}`, (msg) => {
//                 const data = JSON.parse(msg.body);
//
//                 // payload가 JSON 문자열이면 파싱
//                 let payload;
//                 try {
//                     payload = JSON.parse(data.payload);
//                 } catch {
//                     payload = data.payload;
//                 }
//
//                 // 새 알림 객체 생성 (렌더링 포맷에 맞춰 변환)
//                 const newNotif = {
//                     id: data.id || Date.now(),
//                     type: data.type,
//                     title: "조직 초대 알림",
//                     message: `${payload.sender}님이 ${payload.organization} 조직에 초대했습니다.`,
//                     link: payload.link || "#",
//                     createdAt: new Date().toLocaleString(),
//                     isRead: false
//                 };
//
//                 // 현재 알림 배열에 추가 (기존 더미 포함)
//                 notifications.unshift(newNotif);
//                 renderNotifs(notifications);
//
//                 // 시각적 강조 (간단한 반짝 효과)
//                 const firstCard = document.querySelector(".notif-card");
//                 if (firstCard) {
//                     firstCard.style.backgroundColor = "#eaf1ff";
//                     setTimeout(() => firstCard.style.backgroundColor = "#f9f9f9", 2000);
//                 }
//             });
//         });
//     }
//
//     // connectNotificationSocket();
//
//     // 초기 렌더링
//     renderNotifs(notifications);
//
// });
document.addEventListener("DOMContentLoaded", () => {

    /** ======================================
     *  [1] 알림 목록 영역 선택 (없을 수도 있음)
     * ====================================== */
    const notifList = document.getElementById("notifList");
    if (!notifList) {
        console.warn("notifList 요소가 없습니다. (조직 미소속 사용자)");
    }

    /** ======================================
     *  [2] 전역 알림 데이터 (더미 or 서버 연동)
     * ====================================== */
    window.notifications = [
        {
            id: 101,
            type: "PROJECT_INVITE",
            title: "프로젝트 알림",
            message: "새 프로젝트에 초대되었습니다.",
            link: "/projects/12/invite",
            createdAt: "2025-09-25 14:30",
            isRead: false
        },
        {
            id: 102,
            type: "TASK_UPDATE",
            title: "프로젝트 알림",
            message: "태스크 상태가 변경되었어요.",
            link: "/projects/12/tasks/34",
            createdAt: "2025-09-25 15:00",
            isRead: true
        },
        {
            id: 103,
            type: "CALENDAR_EVENT",
            title: "일정 알림",
            message: "‘업무 회의’ 일정이 10분 뒤에 시작돼요.",
            link: "/calendar/56",
            createdAt: "2025-09-25 16:00",
            isRead: false
        }
    ];


    /** ======================================
     *  [3] 알림 렌더링 함수
     * ====================================== */
    const renderNotifs = (data) => {
        // notifList가 없으면 렌더링 스킵
        if (!notifList) return;

        notifList.innerHTML = "";
        data.forEach(n => {
            notifList.innerHTML += `
                <a href="${n.link}" class="text-decoration-none text-dark" data-id="${n.id}">
                  <div class="notif-card ${n.isRead ? 'opacity-75' : ''}">
                    <div class="fw-bold">${n.title}</div>
                    <div>${n.message}</div>
                    <div class="text-end text-muted small">${n.createdAt}</div>
                  </div>
                </a>
            `;
        });
    };


    /** ======================================
     *  [4] 알림 클릭 시 읽음 처리
     * ====================================== */
    if (notifList) {
        notifList.addEventListener("click", (e) => {
            const notifCard = e.target.closest(".notif-card");
            if (!notifCard) return;

            const id = parseInt(notifCard.dataset.id, 10);
            const notif = notifications.find(n => n.id === id);

            if (notif && !notif.isRead) {
                notif.isRead = true; // UI 반영
                renderNotifs(notifications);

                // TODO: 서버 연동 (읽음 처리)
                // fetch(`/api/notifications/${id}/read`, { method: "POST" })
                //     .then(res => {
                //         if (res.ok) console.log("알림 읽음 처리 완료");
                //         else console.error("읽음 처리 실패", res.status);
                //     })
                //     .catch(err => console.error("에러 발생", err));
            }
        });
    }


    /** ======================================
     *  [5] WebSocket 실시간 알림 연결
     * ====================================== */
    window.connectNotificationSocket = function (userId) {
        const socket = new SockJS(`${window.APP_CTX}/ws`);
        // const client = Stomp.over(socket);

        const client = new StompJs.Client({
            webSocketFactory: () => socket,
            debug: (str) => console.log(str),
            reconnectDelay: 5000,
        });

        client.onConnect = () => {
            console.log("Notification WebSocket connected");

            // 개인별 채널 구독
            console.log("✅ Subscribing to:", `/topic/notifications/${userId}`);
            client.subscribe(`/topic/notifications/${userId}`, (msg) => {
                console.log("📨 RAW MSG:", msg);
                const data = JSON.parse(msg.body);

                // payload가 JSON 문자열이면 파싱
                let payload;
                try {
                    payload = JSON.parse(data.payload);
                } catch {
                    payload = data.payload;
                }

                // 새 알림 객체 생성
                const newNotif = {
                    id: data.id || Date.now(),
                    type: data.type,
                    title: "조직 초대 알림",
                    message: `${payload.sender}님이 ${payload.organization} 조직에 초대했습니다.`,
                    link: payload.link || "#",
                    createdAt: new Date().toLocaleString(),
                    isRead: false
                };

                // 배열에 추가 및 렌더링
                notifications.unshift(newNotif);
                renderNotifs(notifications);

                // 반짝 효과
                const firstCard = document.querySelector(".notif-card");
                if (firstCard) {
                    firstCard.style.backgroundColor = "#eaf1ff";
                    setTimeout(() => firstCard.style.backgroundColor = "#f9f9f9", 2000);
                }

                // notifList가 없을 경우 (조직 미소속)
                if (!notifList) {
                    console.log("🔔 새 알림 수신:", newNotif.message);
                    alert("새로운 알림이 도착했습니다!");
                }
            });
        };

        client.activate();
    };


    /** ======================================
     *  [6] 초기 렌더링
     * ====================================== */
    renderNotifs(notifications);
});
