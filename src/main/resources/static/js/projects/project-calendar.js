// ====== Project Calendar (FullCalendar + Modal + 멤버선택) ======
if (!window.initProjectCalendar) {
  window.initProjectCalendar = function () {
    // 중복 초기화 가드
    if (window.__projectCalendarInitDone) return;
    window.__projectCalendarInitDone = true;

    const calendarEl = document.getElementById('calendar');
    if (!calendarEl) return;

    const container = document.getElementById('calendar-container');
    const projectId = container?.dataset.projectId;
    const ctx = (container?.dataset.ctx || '').replace(/\/$/, '');

    // ---- 폼/모달 엘리먼트 ----
    const modalTitle = document.getElementById('modal-title');
    const eventForm = document.getElementById('event-form');
    const btnDelete = document.getElementById('btn-delete');
    const btnSave = document.getElementById('btn-save');
    const allDayCheckbox = document.getElementById('event-all-day');
    const startInput = document.getElementById('event-start');
    const endInput = document.getElementById('event-end');
    const daySelector = document.querySelector('.day-selector-group');
    const relatedTaskSelect = document.getElementById('event-related-task');

    // ---- 참석자: 프로젝트 멤버 리스트 ----
    const memberListEl   = document.getElementById('member-list');
    const memberFilterEl = document.getElementById('member-filter');
    const memberCheckAll = document.getElementById('member-check-all');
    const selectedAttendees = new Set(); // userId Set (string)
    let allMembers = [];                 // [{id,name,email,title,teamName,avatarUrl}]

    // ---- 유틸 ----
    const debounce = (fn, ms = 200) => { let t; return (...a) => { clearTimeout(t); t = setTimeout(() => fn(...a), ms); }; };
    const toLocalDate = (d) => new Date(d - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 10);
    const toLocalDateTime = (d) => new Date(d - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 16);

    // ---- API 래퍼 ----
    const api = {
      // 해당 프로젝트의 태스크만 간단 정보(id, title)로 받아오기
      tasksMinimal: () =>
        fetch(`${ctx}/api/projects/${projectId}/tasks/minimal`, { cache: 'no-cache' })
          .then(r => (r.ok ? r.json() : []))
          .then(arr => (Array.isArray(arr) ? arr.map(t => ({ id: t.id, title: t.title })) : [])),

      projectMembers: () =>
        fetch(`${ctx}/api/projects/${projectId}/members`, { cache: 'no-cache' })
          .then(r => (r.ok ? r.json() : [])),

      // ✅ 백엔드: GET /attendees → [1,5,9] 혹은 [{user:{id}}]
      attendeesList: (eventId) =>
        fetch(`${ctx}/api/projects/${projectId}/calendar/events/${eventId}/attendees`, { cache: 'no-cache' })
          .then(r => (r.ok ? r.json() : []))
          .then(list => {
            if (!Array.isArray(list)) return [];
            if (typeof list[0] === 'number') return list;
            return list
              .map(x => x?.user?.id ?? x?.id ?? null)
              .filter(v => typeof v === 'number');
          }),

      // ✅ 백엔드: PUT /attendees (application/json) ← [1,5,9]
      attendeesReplace: (eventId, userIds) =>
        fetch(`${ctx}/api/projects/${projectId}/calendar/events/${eventId}/attendees`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(userIds)
        }).then(r => { if (!r.ok) throw new Error('attendeesReplace'); return true; }),
    };

    // ---- 팝업 열기/닫기 ----
    function openPopup() {
      const el = document.getElementById('event-popup');
      el.style.display = 'flex';
      el.classList.add('is-open');
    }
    function closePopup() {
      const el = document.getElementById('event-popup');
      el.classList.remove('is-open');
      el.style.display = 'none';
    }
    document.addEventListener('keydown', (e) => { if (e.key === 'Escape') closePopup(); });

    // ---- 태스크 옵션 채우기 ----
    api.tasksMinimal()
      .then(list => {
        list.forEach(t => {
          const opt = document.createElement('option');
          opt.value = t.id;
          opt.textContent = `#${t.id} ${t.title}`;
          relatedTaskSelect.appendChild(opt);
        });
      })
      .catch(() => {});

    // ---- 멤버 목록 최초 로드 ----
    api.projectMembers()
      .then(list => { allMembers = list || []; renderMemberList(''); })
      .catch(() => { allMembers = []; renderMemberList(''); });

    // ---- 멤버 리스트 렌더링/필터/체크 ----
    function renderMemberList(filter = '') {
      const q = filter.trim().toLowerCase();
      const data = q
        ? allMembers.filter(m =>
            (m.name || '').toLowerCase().includes(q) ||
            (m.title || '').toLowerCase().includes(q) ||
            (m.teamName || '').toLowerCase().includes(q) ||
            (m.email || '').toLowerCase().includes(q)
          )
        : allMembers;

      memberListEl.innerHTML = '';
      data.forEach(m => {
        const li = document.createElement('li');
        li.style.cssText = 'display:flex;align-items:center;gap:10px;padding:8px;border-radius:8px;';
        li.addEventListener('mouseenter', () => (li.style.background = '#f3f4f6'));
        li.addEventListener('mouseleave', () => (li.style.background = 'transparent'));

        const checked = selectedAttendees.has(String(m.id));
        li.innerHTML = `
          <div style="width:34px;height:34px;border-radius:999px;overflow:hidden;background:#eef2ff;display:grid;place-items:center;flex:0 0 34px">
            ${m.avatarUrl ? `<img src="${m.avatarUrl}" alt="" style="width:100%;height:100%;object-fit:cover">` : (m.name || 'U')[0]}
          </div>
          <div style="flex:1;min-width:0">
            <div style="font-weight:600">${m.name || ''}</div>
            <div style="font-size:12px;color:#6b7280;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">
              ${(m.title || '')}${m.teamName ? ` • ${m.teamName}` : ''}${m.email ? ` • ${m.email}` : ''}
            </div>
          </div>
          <label>
            <input type="checkbox" class="attendee-check" data-id="${m.id}" ${checked ? 'checked' : ''} />
          </label>
        `;
        memberListEl.appendChild(li);
      });

      // 전체선택 상태 동기화 (현재 보이는 항목 기준)
      const visibleIds = data.map(m => String(m.id));
      const allVisibleChecked = visibleIds.length > 0 && visibleIds.every(id => selectedAttendees.has(id));
      memberCheckAll.checked = allVisibleChecked;
    }

    memberListEl.addEventListener('change', (e) => {
      const box = e.target.closest('.attendee-check');
      if (!box) return;
      const id = String(box.dataset.id);
      if (box.checked) selectedAttendees.add(id);
      else selectedAttendees.delete(id);
      renderMemberList(memberFilterEl.value);
    });
    memberFilterEl.addEventListener('input', debounce(() => renderMemberList(memberFilterEl.value), 160));
    memberCheckAll.addEventListener('change', () => {
      const q = memberFilterEl.value.trim().toLowerCase();
      const visible = (q
        ? allMembers.filter(m =>
            (m.name || '').toLowerCase().includes(q) ||
            (m.title || '').toLowerCase().includes(q) ||
            (m.teamName || '').toLowerCase().includes(q) ||
            (m.email || '').toLowerCase().includes(q)
          )
        : allMembers).map(m => String(m.id));

      if (memberCheckAll.checked) visible.forEach(id => selectedAttendees.add(id));
      else visible.forEach(id => selectedAttendees.delete(id));

      renderMemberList(memberFilterEl.value);
    });

    // ---- FullCalendar ----
    const calendar = new FullCalendar.Calendar(calendarEl, {
      initialView: 'dayGridMonth',
      headerToolbar: { left: 'prev,next today', center: 'title', right: 'dayGridMonth,timeGridWeek,timeGridDay' },
      events: `${ctx}/api/projects/${projectId}/calendar/events`,
      locale: 'ko',
      selectable: true,
      selectMirror: true,
      editable: true,
      dayMaxEvents: true,
      displayEventTime: false,

      dateClick: (info) => openModalForNewEvent(info.dateStr),
      select: (info) => { const d = info.startStr.slice(0, 10); openModalForNewEvent(d); calendar.unselect(); },
      dayCellDidMount: (arg) => {
        const isoDate = arg.date.toISOString().slice(0, 10);
        arg.el.querySelector('.fc-daygrid-day-number')
          ?.addEventListener('click', (e) => { e.preventDefault(); openModalForNewEvent(isoDate); });
        arg.el.addEventListener('dblclick', () => openModalForNewEvent(isoDate));
      },
      eventClick: (info) => openModalForExistingEvent(info.event),
      dayCellContent: (info) => info.dayNumberText.replace('일', ''),
    });

    calendar.render();
    closePopup(); // 초기 숨김 확정

    // ---- 모달 오픈(신규) ----
    function openModalForNewEvent(date) {
      eventForm.reset();
      modalTitle.textContent = '일정 추가';
      btnDelete.style.display = 'none';
      document.getElementById('event-id').value = '';
      daySelector.querySelectorAll('.day-btn.active').forEach(b => b.classList.remove('active'));
      relatedTaskSelect.value = '';

      // 참석자 초기화
      selectedAttendees.clear();
      memberFilterEl.value = '';
      renderMemberList('');

      startInput.type = 'datetime-local';
      endInput.type   = 'datetime-local';
      startInput.value = `${date}T09:00`;
      endInput.value   = `${date}T10:00`;

      openPopup();
    }

    // ---- 모달 오픈(기존) ----
    async function openModalForExistingEvent(event) {
      eventForm.reset();
      modalTitle.textContent = '일정 수정';
      btnDelete.style.display = String(event.id).startsWith('T-') ? 'none' : 'block';
      daySelector.querySelectorAll('.day-btn.active').forEach(b => b.classList.remove('active'));

      document.getElementById('event-id').value = event.id;
      document.getElementById('event-title').value = event.title;

      allDayCheckbox.checked = event.allDay;
      handleAllDayChange(event.allDay, event.start, event.end);

      if (event.extendedProps.rrule) {
        const weekdays = ['SU', 'MO', 'TU', 'WE', 'TH', 'FR', 'SA'];
        const byday = event.extendedProps.rrule.split('BYDAY=')[1];
        if (byday) byday.split(',').forEach(d => {
          const idx = weekdays.indexOf(d);
          if (idx !== -1) daySelector.querySelector(`[data-day="${idx}"]`)?.classList.add('active');
        });
      }

      document.getElementById('event-location').value = event.extendedProps.location || '';
      document.getElementById('event-memo').value     = event.extendedProps.memo || '';
      document.getElementById('event-color').value    = event.backgroundColor || 'blue';
      relatedTaskSelect.value = event.extendedProps.relatedTaskId || '';

      // ✅ 참석자 로드
      selectedAttendees.clear();
      try {
        if (!String(event.id).startsWith('T-')) {
          const ids = await api.attendeesList(event.id); // [1,5,9]
          (ids || []).forEach(id => selectedAttendees.add(String(id)));
        }
      } catch {}
      memberFilterEl.value = '';
      renderMemberList('');

      openPopup();
    }

    // ---- 종일 토글 ----
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

    // ---- 반복 요일 선택 ----
    daySelector.addEventListener('click', (ev) => {
      if (ev.target.classList.contains('day-btn')) ev.target.classList.toggle('active');
    });

    // ---- 저장(생성/수정) ----
    btnSave.addEventListener('click', async function (e) {
      e.preventDefault();

      // 시간 검증
      if (!allDayCheckbox.checked) {
        const st = new Date(startInput.value), en = new Date(endInput.value);
        if (isFinite(st) && isFinite(en) && en <= st) {
          showToast('종료일은 시작일 이후여야 합니다.', 'error', { position: 'bottom-end' });
          return;
        }
      }

      const eventId = document.getElementById('event-id').value;
      const selectedDays = Array.from(daySelector.querySelectorAll('.day-btn.active')).map(b => b.dataset.day);
      const weekdays = ['SU', 'MO', 'TU', 'WE', 'TH', 'FR', 'SA'];
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
        showToast('태스크 일정은 태스크 화면에서 수정하세요.', 'warning');
        closePopup();
        return;
      }

      const method = eventId ? 'PUT' : 'POST';
      const url = eventId
        ? `${ctx}/api/projects/${projectId}/calendar/events/${eventId}`
        : `${ctx}/api/projects/${projectId}/calendar/events`;

      try {
        const res = await fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
        if (!res.ok) throw new Error('save failed');
        const saved = await res.json(); // EventDto
        const savedId = saved?.id || eventId;

        // ✅ 참석자 교체
        if (savedId && !String(savedId).startsWith('T-')) {
          await api.attendeesReplace(savedId, [...selectedAttendees].map(Number));
        }

        calendar.refetchEvents();
        showToast(eventId ? '일정이 수정되었습니다.' : '일정이 등록되었습니다.', 'success', { duration: 2500, position: 'bottom-end' });
      } catch (err) {
        showToast('처리에 실패했습니다. 다시 시도해주세요.', 'error', { position: 'bottom-end' });
      } finally {
        closePopup();
      }
    });

    // ---- 삭제 ----
    btnDelete.addEventListener('click', function () {
      const eventId = document.getElementById('event-id').value;
      fetch(`${ctx}/api/projects/${projectId}/calendar/events/${eventId}`, { method: 'DELETE' })
        .then(() => {
          calendar.refetchEvents();
          showToast('일정이 삭제되었습니다.', 'warning', { duration: 2500, position: 'bottom-end' });
        })
        .finally(closePopup);
    });

    // ---- 모달 바깥 클릭/닫기 버튼 ----
    const popup = document.getElementById('event-popup');
    document.querySelectorAll('.popup-close').forEach(b => b.addEventListener('click', closePopup));
    popup.addEventListener('click', (e) => { if (e.target === popup) closePopup(); });

    // ---- 토스트 ----
    function showToast(message = '완료되었습니다.', type = 'success', opts = {}) {
      const el = document.getElementById('planToast'); if (!el) return;
      const container = el.closest('.toast-container');
      const pos = opts.position || 'top-end';
      container.className = `toast-container position-fixed p-3 ${pos.includes('bottom') ? 'bottom-0' : 'top-0'} ${pos.includes('start') ? 'start-0' : 'end-0'}`;
      el.className = 'toast clean-toast'; el.classList.add(`toast-${type}`);
      el.querySelector('.toast-body').textContent = message;
      el.querySelector('.toast-icon').textContent = ({ success: '✔', error: '✖', warning: '!', info: 'ℹ' })[type] || 'ℹ';
      const delay = Number(opts.duration || 2200);
      const t = bootstrap.Toast.getOrCreateInstance(el, { autohide: true, delay });
      t.show();
    }
  };
}
