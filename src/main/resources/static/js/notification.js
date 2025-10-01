document.addEventListener("DOMContentLoaded", () => {
    const notifList = document.getElementById("notifList");

    // 더미 JSON (나중에 fetch("/api/notifications")로 교체)
    // 백엔드에서 payload json으로 메세지까지 함께 내려주세요.
    const notifications = [
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

    //  렌더링 함수
    const renderNotifs = (data) => {
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


    //  알림 클릭 시 읽음 처리
    notifList.addEventListener("click", (e) => {
        const notifItem = e.target.closest(".notif-item");
        if (!notifItem) return;

        const id = parseInt(notifItem.dataset.id, 10);
        const notif = notifications.find(n => n.id === id);

        if (notif && !notif.isRead) {
            notif.isRead = true; // UI 반영
            renderNotifs(notifications);

            // TODO: 서버 연동 (읽음 처리)
            // 근데 알림은 RESTful보다 액션 엔드포인트가 나을듯.. 주소는 임시
            // fetch(`/api/notifications/${id}/read`, { method: "POST" })
            //     .then(res => {
            //         if (res.ok) console.log("알림 읽음 처리 완료");
            //         else console.error("읽음 처리 실패", res.status);
            //     })
            //     .catch(err => console.error("에러 발생", err));
        }
    });


    // 초기 렌더링
    renderNotifs(notifications);
});
