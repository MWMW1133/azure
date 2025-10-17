/** ======================================
 *  [1] 전역 알림 데이터 (더미 or 서버 연동)
 * ====================================== */
window.notifications = [];

/** ======================================
 *  [2] 알림 렌더링 함수
 * ====================================== */
function renderNotifs(data) {
  const notifList = document.getElementById('notifList');
  if (!notifList) return;
  notifList.innerHTML = '';

  data.forEach((n) => {
    let actionsHtml = '';
    if (n.type === 'INVITE_ORGANIZATION') {
      actionsHtml = `
        <div class="mt-2 d-flex gap-2">
          <button class="btn btn-sm btn-primary" onclick="handleInviteAction(${n.id}, 'accept')">수락</button>
          <button class="btn btn-sm btn-outline-secondary" onclick="handleInviteAction(${n.id}, 'reject')">거절</button>
        </div>`;
    }

    const linkHtml = (n.link && n.link !== '#')
      ? `<div class="mt-2"><a class="small text-decoration-underline" href="${n.link}">바로가기</a></div>`
      : '';

    notifList.innerHTML += `
      <div class="notif-card ${n.isRead ? 'opacity-75' : ''}" data-id="${n.id}">
        <div class="fw-bold">${n.title}</div>
        <div>${n.message}</div>
        ${linkHtml}
        ${actionsHtml}
        <div class="text-end text-muted small mt-1">${n.createdAt}</div>
      </div>`;
  });
}
renderNotifs(window.notifications || []);

/** ======================================
 *  [3] 알림 클릭 시 읽음 처리 (전역 이벤트 위임)
 * ====================================== */
document.addEventListener('click', (e) => {
  const notifCard = e.target.closest('.notif-card');
  if (!notifCard) return;

  const id = parseInt(notifCard.dataset.id, 10);
  const notif = window.notifications.find((n) => n.id === id);

  if (notif && !notif.isRead) {
    notif.isRead = true;
    renderNotifs(window.notifications);
  }

  // 선택: 카드 클릭 시 링크가 있으면 이동
  if (notif?.link && notif.link !== '#') {
    // 존재하지 않는 경로면 홈으로 리다이렉트
    if (notif.link === '/organization/invitations') {
      window.location.href = '/home';      // 또는 '/noInvitePage'
    } else {
      window.location.href = notif.link;
    }
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
    console.log('Notification WebSocket connected');
    client.subscribe(`/topic/notifications/${userId}`, (msg) => {
      console.log('[STEP3] raw msg:', msg.body);
      const data = JSON.parse(msg.body);
      console.log('[STEP3] parsed data:', data);
      console.log('[STEP3] typeof payload:', typeof data.payload);
      console.log('[STEP3] payload content:', data.payload);

      // payload 원본은 그대로 유지
      const payload = data.payload;

      // 견고성: 문자열일 수도 있으니 별도의 객체 변수에만 파싱 적용
      let payloadObj = payload;
      if (typeof payload === 'string') {
        try { payloadObj = JSON.parse(payload); } catch (e) {}
      }

      // 타입 정규화(대소문자/공백 문제 방지)
      const type = String(data.type || '').trim().toUpperCase();
      console.log('[TYPE]', type);

      switch (type) {
        case 'INVITE_ORGANIZATION': {
          window.notifications.unshift({
            id: data.id || Date.now(),
            type,
            payload: payloadObj,
            title: '조직 초대 알림',
            message: `${payloadObj?.sender ?? '관리자'}님이 ${payloadObj?.organization ?? ''} 조직에 초대했습니다.`,
            organizationId: payloadObj?.organizationId,
            link: payloadObj?.link || '#',
            createdAt: new Date().toLocaleString(),
            isRead: false,
          });
          break;
        }

        case 'TASK_WORKFLOW_CHANGED': {
          window.notifications.unshift({
            id: data.id || Date.now(),
            type,
            payload: payloadObj,
            title: '태스크 상태 변경 알림',
            message: `[${payloadObj?.taskTitle ?? '태스크'}] 상태: ${payloadObj?.from ?? '-'} → ${payloadObj?.to ?? '-'} (변경자: ${payloadObj?.sender ?? '-'})`,
            link: payloadObj?.link || '#',
            createdAt: new Date().toLocaleString(),
            isRead: false,
          });
          break;
        }

        case 'PROJECT_MEMBER_ADDED': {
          // 멤버로 추가됨 알림 (콘솔에서 이 타입이 보였음)
          const message = `${payloadObj?.adderName ?? '관리자'}님이 '${payloadObj?.projectName ?? '프로젝트'}'에 당신을 추가했습니다.`;
          let link = '#';
          if (payloadObj?.projectId) {
            link = `${window.APP_CTX || ''}/projects/${payloadObj.projectId}`;
          }
          window.notifications.unshift({
            id: data.id || Date.now(),
            type,
            payload: payloadObj,
            title: '프로젝트 멤버로 추가됨',
            message,
            organizationId: payloadObj?.organizationId,
            link,
            createdAt: new Date().toLocaleString(),
            isRead: false,
          });
          break;
        }

        case 'PROPOSAL_STATUS_CHANGED': {
          // 서버 payload 예: { proposalId, projectId, organizationId, status, name, message }
          const status = payloadObj?.status; // APPROVED | REJECTED
          const pname  = payloadObj?.name || '제안';
          const title  = '프로젝트 제안 상태 변경';
          const message = payloadObj?.message
            || (status === 'APPROVED'
                ? `‘${pname}’ 제안이 승인되었습니다.`
                : `‘${pname}’ 제안이 거절되었습니다.`);

          let link = '#';
          if (status === 'APPROVED' && payloadObj?.projectId) {
            link = `${window.APP_CTX || ''}/projects/${payloadObj.projectId}`;
          } else if (payloadObj?.proposalId) {
            link = `${window.APP_CTX || ''}/project-plan?proposalId=${payloadObj.proposalId}`;
          } else {
            link = `${window.APP_CTX || ''}/project-plan`;
          }

          window.notifications.unshift({
            id: data.id || Date.now(),
            type,
            payload: payloadObj,
            title,
            message,
            organizationId: payloadObj?.organizationId,
            link,
            createdAt: new Date().toLocaleString(),
            isRead: false,
          });
          break;
        }

        default: {
          window.notifications.unshift({
            id: data.id || Date.now(),
            type,
            payload: payloadObj,
            title: '새 알림',
            message: payloadObj?.message || '새로운 알림이 있습니다.',
            createdAt: new Date().toLocaleString(),
            isRead: false,
          });
        }
      }

      renderNotifs(window.notifications);

      // 시각 효과 (벨 색상 + 첫 카드 하이라이트)
      const bellIcon = document.querySelector('.bi-bell');
      if (bellIcon) {
        bellIcon.classList.add('text-danger');
        setTimeout(() => bellIcon.classList.remove('text-danger'), 2000);
      }

      const firstCard = document.querySelector('.notif-card');
      if (firstCard) {
        firstCard.style.backgroundColor = '#eaf1ff';
        setTimeout(() => (firstCard.style.backgroundColor = '#f9f9f9'), 2000);
      }

      // notifList가 없는 경우 (탑바 없는 페이지)
      const notifList = document.getElementById('notifList');
      if (!notifList) {
        alert('새로운 알림이 도착했습니다!');
      }
    });
  };
  client.activate();
};

/** ======================================
 *  [5] 초대 수락/거절 처리
 * ====================================== */
window.handleInviteAction = function (notifId, action) {
  const notif = window.notifications.find((n) => n.id === notifId);
  if (!notif) return;

  const orgId = notif.payload?.organizationId;
  if (!orgId) {
    alert('organizationId가 없습니다. (payload 오류)');
    return;
  }

  const bodyData = { organizationId: orgId };

  console.log('[FETCH] sending body =', bodyData);
  console.log('[FETCH] JSON.stringify =', JSON.stringify(bodyData));


  fetch(`/api/invite/${action}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(bodyData),
  })
      .then((res) => res.text()) // ← JSON 대신 text로 받아서
      .then((text) => {
        let data;
        try {
          data = JSON.parse(text);
        } catch {
          data = { message: text || '요청이 완료되었습니다.' };
        }

        alert(data.message);
        window.notifications = window.notifications.filter((n) => n.id !== notifId);
        renderNotifs(window.notifications);

        if (action === 'accept') {
          window.location.href = '/home';
        } else if (action === 'reject') {
          window.location.href = '/noInvitePage';
        }
      })
      .catch((err) => console.warn('[INVITE FETCH ERROR]', err));

};