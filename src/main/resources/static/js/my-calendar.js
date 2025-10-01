window.initCalendar = function () {
  // --- 요소 가져오기 ---
  const calendarEl = document.getElementById('calendar');
  if (!calendarEl) return; // DOM이 없으면 실행 안 함

  const modalTitle = document.getElementById('modal-title');
  const eventForm = document.getElementById('event-form');
  const btnDelete = document.getElementById('btn-delete');
  const btnSave = document.getElementById('btn-save');
  const allDayCheckbox = document.getElementById('event-all-day');
  const startInput = document.getElementById('event-start');
  const endInput = document.getElementById('event-end');
  const daySelector = document.querySelector('.day-selector-group');

  // --- 커스텀 팝업 열기/닫기 함수 ---
  function openPopup() {
    const popup = document.getElementById('event-popup');
    if (popup) popup.style.display = 'block';
  }
  function closePopup() {
    const popup = document.getElementById('event-popup');
    if (popup) popup.style.display = 'none';
  }

  // --- FullCalendar 초기화 ---
  const calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: 'dayGridMonth',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay',
    },
    events: '/api/calendar/events', // 서버에서 JSON 형태로 이벤트 목록 불러오기
    locale: 'ko',
    selectable: true,
    editable: true,
    dayMaxEvents: true,
    displayEventTime: false,
    eventDisplay: 'block',
    dateClick: function (info) {
      openModalForNewEvent(info.dateStr);
    },
    eventClick: function (info) {
      openModalForExistingEvent(info.event);
    },
    dayCellContent: function (info) {
      return info.dayNumberText.replace('일', '');
    },
  });

  calendar.render();

  // --- 팝업 관련 함수 ---
  function openModalForNewEvent(date) {
    eventForm.reset();
    modalTitle.textContent = '일정 추가';
    btnDelete.style.display = 'none';
    document.getElementById('event-id').value = '';
    daySelector.querySelectorAll('.day-btn.active').forEach((b) => b.classList.remove('active'));

    startInput.type = 'datetime-local';
    endInput.type = 'datetime-local';
    startInput.value = date + 'T09:00';
    endInput.value = date + 'T10:00';

    openPopup();
  }

  function openModalForExistingEvent(event) {
    eventForm.reset();
    modalTitle.textContent = '일정 수정';
    btnDelete.style.display = 'block';
    daySelector.querySelectorAll('.day-btn.active').forEach((b) => b.classList.remove('active'));

    document.getElementById('event-id').value = event.id;
    document.getElementById('event-title').value = event.title;

    allDayCheckbox.checked = event.allDay;
    handleAllDayChange(event.allDay, event.start, event.end);

    // 반복 요일 정보 (rrule 파싱)
    if (event.extendedProps.rrule) {
      const weekdays = ['SU', 'MO', 'TU', 'WE', 'TH', 'FR', 'SA'];
      const byday = event.extendedProps.rrule.split('BYDAY=')[1];
      if (byday) {
        byday.split(',').forEach((day) => {
          const dayIndex = weekdays.indexOf(day);
          if (dayIndex !== -1) {
            daySelector.querySelector(`[data-day="${dayIndex}"]`)?.classList.add('active');
          }
        });
      }
    }

    document.getElementById('event-location').value = event.extendedProps.location || '';
    document.getElementById('event-memo').value = event.extendedProps.memo || '';
    document.getElementById('event-color').value = event.backgroundColor || 'blue';

    openPopup();
  }

  // '종일' 체크박스 이벤트
  allDayCheckbox.addEventListener('change', () => handleAllDayChange(allDayCheckbox.checked));

  function handleAllDayChange(isAllDay, startDate, endDate) {
    startDate = startDate || new Date(startInput.value || Date.now());
    endDate = endDate || new Date(endInput.value || startDate);

    if (isAllDay) {
      startInput.type = 'date';
      endInput.type = 'date';
      startInput.value = new Date(startDate - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 10);
      endInput.value = new Date(endDate - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 10);
    } else {
      startInput.type = 'datetime-local';
      endInput.type = 'datetime-local';
      startInput.value = new Date(startDate - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 16);
      endInput.value = new Date(endDate - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 16);
    }
  }

  // 요일 버튼 클릭 이벤트
  daySelector.addEventListener('click', function (event) {
    if (event.target.classList.contains('day-btn')) {
      event.target.classList.toggle('active');
    }
  });

  // '완료' 버튼 클릭 이벤트 (저장/수정)
  btnSave.addEventListener('click', function (e) {
    e.preventDefault();

    const eventId = document.getElementById('event-id').value;
    const selectedDayButtons = daySelector.querySelectorAll('.day-btn.active');
    const selectedDays = Array.from(selectedDayButtons).map((button) => button.dataset.day);
    const weekdays = ['SU', 'MO', 'TU', 'WE', 'TH', 'FR', 'SA'];
    const bydayString = selectedDays.map((day) => weekdays[day]).join(',');

    const eventData = {
      id: eventId || null,
      title: document.getElementById('event-title').value,
      start: startInput.value,
      end: endInput.value,
      allDay: allDayCheckbox.checked,
      reminder: document.getElementById('event-reminder').value,
      extendedProps: {
        location: document.getElementById('event-location').value,
        memo: document.getElementById('event-memo').value,
        rrule: bydayString ? `FREQ=WEEKLY;BYDAY=${bydayString}` : null,
      },
      backgroundColor: document.getElementById('event-color').value,
    };

    if (eventId) {
      fetch(`/api/calendar/events/${eventId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(eventData),
      })
        .then((res) => {
          if (!res.ok) throw new Error('PUT failed');
          return res.json(); // 응답 본문 안 쓰면 생략해도 됨
        })
        .then(() => {
          calendar.refetchEvents();
          showToast('일정이 수정되었습니다.', 'success', { duration: 2500, position: 'bottom-end' });
        })
        .catch(() => {
          showToast('수정에 실패했습니다. 다시 시도해주세요.', 'error', { position: 'bottom-end' });
        })
        .finally(closePopup);
    } else {
      fetch('/api/calendar/events', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(eventData),
      })
        .then((res) => {
          if (!res.ok) throw new Error('POST failed');
          return res.json();
        })
        .then(() => {
          calendar.refetchEvents();
          showToast('일정이 등록되었습니다.', 'success', { duration: 2500, position: 'bottom-end' });
        })
        .catch(() => {
          showToast('등록에 실패했습니다. 다시 시도해주세요.', 'error', { position: 'bottom-end' });
        })
        .finally(closePopup);
    }
  });

  // '삭제' 버튼 클릭 이벤트
  btnDelete.addEventListener('click', function () {
    const eventId = document.getElementById('event-id').value;

    fetch(`/api/calendar/events/${eventId}`, { method: 'DELETE' })
      .then(() => calendar.refetchEvents(), showToast('일정이 삭제되었습니다.', 'warning', { duration: 2500, position: 'bottom-end' }))
      .finally(closePopup);
  });

  // 팝업 닫기 버튼 처리
  const popup = document.getElementById('event-popup');
  const closeButtons = document.querySelectorAll('.popup-close');

  closeButtons.forEach((btn) => {
    btn.addEventListener('click', () => {
      popup.style.display = 'none';
    });
  });

  // 팝업 배경 클릭 시 닫기
  popup.addEventListener('click', (e) => {
    if (e.target === popup) {
      popup.style.display = 'none';
    }
  });

  function showToast(message = '완료되었습니다.', type = 'success', opts = {}) {
    const el = document.getElementById('planToast');
    if (!el) return;

    // 위치 설정
    const container = el.closest('.toast-container');
    const pos = opts.position || 'top-end';
    container.className = `toast-container position-fixed p-3 ${pos.includes('bottom') ? 'bottom-0' : 'top-0'} ${pos.includes('start') ? 'start-0' : 'end-0'}`;

    // 클래스/메시지/아이콘
    el.className = 'toast clean-toast';
    el.classList.add(`toast-${type}`);
    el.querySelector('.toast-body').textContent = message;

    const iconEl = el.querySelector('.toast-icon');
    const icons = { success: '✔', error: '✖', warning: '!', info: 'ℹ' };
    iconEl.textContent = icons[type] || 'ℹ';

    // Bootstrap 토스트
    const delay = Number(opts.duration || 2200);
    const t = bootstrap.Toast.getOrCreateInstance(el, { autohide: true, delay });
    t.show();
  }
};
