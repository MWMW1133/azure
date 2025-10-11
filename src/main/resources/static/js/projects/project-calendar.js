window.initProjectCalendar = function () {
  const calendarEl = document.getElementById('calendar');
  if (!calendarEl) return;

  const container = document.getElementById('calendar-container');
  const projectId = container?.dataset.projectId;
  const ctx = (container?.dataset.ctx || '').replace(/\/$/, '');

  const modalTitle = document.getElementById('modal-title');
  const eventForm = document.getElementById('event-form');
  const btnDelete = document.getElementById('btn-delete');
  const btnSave = document.getElementById('btn-save');
  const allDayCheckbox = document.getElementById('event-all-day');
  const startInput = document.getElementById('event-start');
  const endInput = document.getElementById('event-end');
  const daySelector = document.querySelector('.day-selector-group');
  const relatedTaskSelect = document.getElementById('event-related-task');

  // 태스크 목록 로드(선택)
  fetch(`${ctx}/api/projects/${projectId}/tasks/minimal`)
    .then(r => r.ok ? r.json() : [])
    .then(list => {
      list.forEach(t => {
        const opt = document.createElement('option');
        opt.value = t.id;
        opt.textContent = `#${t.id} ${t.title}`;
        relatedTaskSelect.appendChild(opt);
      });
    }).catch(()=>{});

  function openPopup(){ document.getElementById('event-popup').classList.add('is-open'); }
    function closePopup(){ document.getElementById('event-popup').classList.remove('is-open'); }


  const calendar = new FullCalendar.Calendar(calendarEl, {
  initialView: 'dayGridMonth',
  headerToolbar: { left:'prev,next today', center:'title', right:'dayGridMonth,timeGridWeek,timeGridDay' },
  events: `${ctx}/api/projects/${projectId}/calendar/events`,
  locale: 'ko',
  selectable: true,        // ⬅ 드래그 선택 가능
  selectMirror: true,
  editable: true,
  dayMaxEvents: true,
  displayEventTime: false,

  // ⬇ 날짜 한 번 클릭으로 새 일정 팝업
  dateClick: (info) => openModalForNewEvent(info.dateStr),

  // ⬇ 마우스로 범위 드래그 선택해도 팝업
  select: (info) => {
    const d = info.startStr.slice(0,10);
    openModalForNewEvent(d);
    calendar.unselect();
  },

  // ⬇ 일자 숫자 클릭/셀 더블클릭도 팝업
  dayCellDidMount: (arg) => {
    const isoDate = arg.date.toISOString().slice(0,10);
    arg.el.querySelector('.fc-daygrid-day-number')
      ?.addEventListener('click', (e) => { e.preventDefault(); openModalForNewEvent(isoDate); });
    arg.el.addEventListener('dblclick', () => openModalForNewEvent(isoDate));
  },

  eventClick: (info) => openModalForExistingEvent(info.event),
  eventDrop: async (info) => { /* ← 기존 코드 그대로 유지 */ },
  eventResize: async (info) => { /* ← 기존 코드 그대로 유지 */ },
  dayCellContent: (info) => info.dayNumberText.replace('일','')
});

  calendar.render();

  function openModalForNewEvent(date) {
    eventForm.reset();
    modalTitle.textContent = '일정 추가';
    btnDelete.style.display = 'none';
    document.getElementById('event-id').value = '';
    daySelector.querySelectorAll('.day-btn.active').forEach(b => b.classList.remove('active'));
    relatedTaskSelect.value = '';
    startInput.type = 'datetime-local';
    endInput.type = 'datetime-local';
    startInput.value = date + 'T09:00';
    endInput.value = date + 'T10:00';
    openPopup();
  }

  function openModalForExistingEvent(event) {
    eventForm.reset();
    modalTitle.textContent = '일정 수정';
    btnDelete.style.display = String(event.id).startsWith('T-') ? 'none' : 'block';
    daySelector.querySelectorAll('.day-btn.active').forEach(b => b.classList.remove('active'));

    document.getElementById('event-id').value = event.id;
    document.getElementById('event-title').value = event.title;

    allDayCheckbox.checked = event.allDay;
    handleAllDayChange(event.allDay, event.start, event.end);

    if (event.extendedProps.rrule) {
      const weekdays = ['SU','MO','TU','WE','TH','FR','SA'];
      const byday = event.extendedProps.rrule.split('BYDAY=')[1];
      if (byday) byday.split(',').forEach(d => {
        const idx = weekdays.indexOf(d);
        if (idx !== -1) daySelector.querySelector(`[data-day="${idx}"]`)?.classList.add('active');
      });
    }

    document.getElementById('event-location').value = event.extendedProps.location || '';
    document.getElementById('event-memo').value = event.extendedProps.memo || '';
    document.getElementById('event-color').value = event.backgroundColor || 'blue';
    relatedTaskSelect.value = event.extendedProps.relatedTaskId || '';

    openPopup();
  }

  allDayCheckbox.addEventListener('change', () => handleAllDayChange(allDayCheckbox.checked));
  function handleAllDayChange(isAllDay, startDate, endDate) {
    startDate = startDate || new Date(startInput.value || Date.now());
    endDate   = endDate   || new Date(endInput.value   || startDate);
    if (isAllDay) {
      startInput.type = 'date'; endInput.type = 'date';
      startInput.value = toLocalDate(startDate); endInput.value = toLocalDate(endDate);
    } else {
      startInput.type = 'datetime-local'; endInput.type = 'datetime-local';
      startInput.value = toLocalDateTime(startDate); endInput.value = toLocalDateTime(endDate);
    }
  }
  function toLocalDate(d){ return new Date(d - new Date().getTimezoneOffset()*60000).toISOString().slice(0,10); }
  function toLocalDateTime(d){ return new Date(d - new Date().getTimezoneOffset()*60000).toISOString().slice(0,16); }

  daySelector.addEventListener('click', (ev) => {
    if (ev.target.classList.contains('day-btn')) ev.target.classList.toggle('active');
  });

  btnSave.addEventListener('click', function (e) {
    e.preventDefault();

    const eventId = document.getElementById('event-id').value;
    const selectedDays = Array.from(daySelector.querySelectorAll('.day-btn.active')).map(b => b.dataset.day);
    const weekdays = ['SU','MO','TU','WE','TH','FR','SA'];
    const bydayString = selectedDays.map(d => weekdays[d]).join(',');

    const payload = {
      id: eventId || null,
      title: document.getElementById('event-title').value,
      start: startInput.value,
      end: endInput.value,
      allDay: allDayCheckbox.checked,
      rrule: bydayString ? `FREQ=WEEKLY;BYDAY=${bydayString}` : null,
      backgroundColor: document.getElementById('event-color').value,
      extendedProps: {
        location: document.getElementById('event-location').value,
        memo: document.getElementById('event-memo').value,
        relatedTaskId: relatedTaskSelect.value || null
      }
    };

    if (eventId && String(eventId).startsWith('T-')) {
      showToast('태스크 일정은 태스크 화면에서 수정하세요.','warning'); closePopup(); return;
    }

    const method = eventId ? 'PUT' : 'POST';
    const url = eventId
      ? `${ctx}/api/projects/${projectId}/calendar/events/${eventId}`
      : `${ctx}/api/projects/${projectId}/calendar/events`;

    fetch(url, { method, headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload) })
      .then(res => { if (!res.ok) throw new Error(); return res.json(); })
      .then(() => { calendar.refetchEvents(); showToast(eventId?'일정이 수정되었습니다.':'일정이 등록되었습니다.','success',{duration:2500,position:'bottom-end'}); })
      .catch(() => showToast('처리에 실패했습니다. 다시 시도해주세요.','error',{position:'bottom-end'}))
      .finally(closePopup);
  });

  btnDelete.addEventListener('click', function () {
    const eventId = document.getElementById('event-id').value;
    fetch(`${ctx}/api/projects/${projectId}/calendar/events/${eventId}`, { method:'DELETE' })
      .then(() => { calendar.refetchEvents(); showToast('일정이 삭제되었습니다.','warning',{duration:2500,position:'bottom-end'}); })
      .finally(closePopup);
  });

  const popup = document.getElementById('event-popup');
  document.querySelectorAll('.popup-close').forEach(b => b.addEventListener('click', () => popup.style.display='none'));
  popup.addEventListener('click', (e)=>{ if (e.target===popup) popup.style.display='none'; });

  function toPayloadFromEvent(e){
    return {
      id: e.id,
      title: e.title,
      start: e.startStr,
      end: e.end ? e.end.toISOString().slice(0,19) : e.startStr,
      allDay: e.allDay,
      rrule: e.extendedProps.rrule || null,
      backgroundColor: e.backgroundColor || 'blue',
      extendedProps: {
        location: e.extendedProps.location || '',
        memo: e.extendedProps.memo || '',
        relatedTaskId: e.extendedProps.relatedTaskId || null
      }
    };
  }

  function showToast(message='완료되었습니다.', type='success', opts={}){
    const el = document.getElementById('planToast'); if (!el) return;
    const container = el.closest('.toast-container');
    const pos = opts.position || 'top-end';
    container.className = `toast-container position-fixed p-3 ${pos.includes('bottom')?'bottom-0':'top-0'} ${pos.includes('start')?'start-0':'end-0'}`;
    el.className = 'toast clean-toast'; el.classList.add(`toast-${type}`);
    el.querySelector('.toast-body').textContent = message;
    el.querySelector('.toast-icon').textContent = ({success:'✔',error:'✖',warning:'!',info:'ℹ'})[type] || 'ℹ';
    const delay = Number(opts.duration || 2200);
    const t = bootstrap.Toast.getOrCreateInstance(el, { autohide:true, delay }); t.show();
  }
};
