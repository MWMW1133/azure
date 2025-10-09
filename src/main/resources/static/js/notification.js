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

    window.renderNotifs = renderNotifs;


    /** ======================================
     *  [4] 알림 클릭 시 읽음 처리
     * ====================================== */
    if (notifList) {
        notifList.addEventListener("click", (e) => {
            const link = e.target.closest('a[data-id]');
            if (!link) return;
            const id = parseInt(link.dataset.id, 10);
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
    // 전역에 노출
window.connectNotificationSocket = function (userId) {
  // 1) 소켓 연결
  const socket = new SockJS((window.APP_CTX || '') + '/ws');

  // 2) Stomp 클라이언트 (legacy)
  const client = Stomp.over(socket);
  // 필요하면 디버그 끄기
  // client.debug = null;

  // 3) 연결
  client.connect(
    {}, // headers
    function onConnect(frame) {
      console.log('Notification WebSocket connected:', frame);

      const dest = `/topic/notifications/${userId}`;
      console.log('✅ Subscribing to:', dest);

      client.subscribe(dest, function (msg) {
        try {
          const data = JSON.parse(msg.body);

          // payload가 JSON 문자열일 수도 있음
          let payload = data.payload;
          if (typeof payload === 'string') {
            try { payload = JSON.parse(payload); } catch {}
          }

          const newNotif = {
            id: data.id || Date.now(),
            type: data.type,
            title: '조직 초대 알림',
            message: `${payload?.sender}님이 ${payload?.organization} 조직에 초대했습니다.`,
            link: payload?.link || '#',
            createdAt: new Date().toLocaleString(),
            isRead: false
          };

          // 전역 배열/렌더러가 있으면 반영
          if (Array.isArray(window.notifications)) {
            window.notifications.unshift(newNotif);
          }
          if (typeof window.renderNotifs === 'function') {
            window.renderNotifs(window.notifications || [newNotif]);
          }

          // 리스트가 없을 때 최소 알림
          const firstCard = document.querySelector('.notif-card');
          if (firstCard) {
            firstCard.style.backgroundColor = '#eaf1ff';
            setTimeout(() => (firstCard.style.backgroundColor = '#f9f9f9'), 2000);
          } else {
            alert('새로운 알림이 도착했습니다!');
          }
        } catch (e) {
          console.error('notification parse error', e, msg?.body);
        }
      });
    },
    function onError(err) {
      console.error('STOMP error:', err);
    }
  );
};
    /** ======================================
     *  [6] 초기 렌더링
     * ====================================== */
    renderNotifs(notifications);
});
