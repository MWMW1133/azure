(function () {
  if (window.__projectCalendarInitDone) return;
  window.__projectCalendarInitDone = true;

  var container = document.getElementById('calendar-container');
  var calendarEl = document.getElementById('calendar');
  if (!container || !calendarEl) return;

  var projectId = (container.dataset.projectId || '').trim();
  var ctx = (container.dataset.ctx || '').replace(/\/$/, '');

  // 폼/모달
  var modalTitle = document.getElementById('modal-title');
  var eventForm = document.getElementById('event-form');
  var btnDelete = document.getElementById('btn-delete');
  var btnSave = document.getElementById('btn-save');
  var allDayCheckbox = document.getElementById('event-all-day');
  var startInput = document.getElementById('event-start');
  var endInput = document.getElementById('event-end');
  var daySelector = document.querySelector('.day-selector-group');
  var relatedTaskSelect = document.getElementById('event-related-task');

  // 참석자
  var memberListEl = document.getElementById('member-list');
  var memberFilterEl = document.getElementById('member-filter');
  var memberCheckAll = document.getElementById('member-check-all');
  var selectedAttendees = new Set();
  var allMembers = [];

  // 유틸
  var debounce = function (fn, ms) { ms = ms || 200; var t; return function () { clearTimeout(t); var a = arguments; t = setTimeout(function () { fn.apply(null, a); }, ms); }; };
  var toLocalDate = function (d) { return new Date(d - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 10); }
  var toLocalDateTime = function (d) { return new Date(d - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 16); }

  // API
  var api = {
    tasksMinimal: function () {
      return fetch(ctx + '/api/projects/' + projectId + '/tasks/minimal', { cache: 'no-cache' })
        .then(function (r) { return r.ok ? r.json() : []; })
        .then(function (arr) { return Array.isArray(arr) ? arr.map(function (t) { return { id: t.id, title: t.title }; }) : []; });
    },
    projectMembers: function () {
      return fetch(ctx + '/api/projects/' + projectId + '/members', { cache: 'no-cache' })
        .then(function (r) { return r.ok ? r.json() : []; });
    },
    attendeesList: function (eventId) {
      return fetch(ctx + '/api/projects/' + projectId + '/calendar/events/' + eventId + '/attendees', { cache: 'no-cache' })
        .then(function (r) { return r.ok ? r.json() : []; })
        .then(function (list) {
          if (!Array.isArray(list)) return [];
          if (typeof list[0] === 'number') return list;
          return list.map(function (x) { return x && x.user && x.user.id ? x.user.id : (x && x.id) ? x.id : null; })
            .filter(function (v) { return typeof v === 'number'; });
        });
    },
    attendeesReplace: function (eventId, userIds) {
      return fetch(ctx + '/api/projects/' + projectId + '/calendar/events/' + eventId + '/attendees', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(userIds)
      }).then(function (r) { if (!r.ok) throw new Error('attendeesReplace'); return true; });
    }
  };

  // 팝업 제어
  function openPopup() {
    var el = document.getElementById('event-popup');
    el.classList.add('is-open');
  }
  function closePopup() {
    var el = document.getElementById('event-popup');
    el.classList.remove('is-open');
  }
  document.addEventListener('keydown', function (e) { if (e.key === 'Escape') closePopup(); });

  // 태스크 옵션
  api.tasksMinimal().then(function (list) {
    (list || []).forEach(function (t) {
      var opt = document.createElement('option');
      opt.value = t.id;
      opt.textContent = '#' + t.id + ' ' + t.title;
      relatedTaskSelect.appendChild(opt);
    });
  }).catch(function () { });

  // 멤버 로드
  api.projectMembers()
    .then(function (list) { allMembers = list || []; renderMemberList(''); })
    .catch(function () { allMembers = []; renderMemberList(''); });

  // ★ JSP EL 충돌 방지: 템플릿 문자열(backticks)와 ${} 미사용
  function renderMemberList(filter) {
    filter = filter || '';
    var q = filter.trim().toLowerCase();
    var data = q
      ? allMembers.filter(function (m) {
        return String(m.name || '').toLowerCase().includes(q) ||
          String(m.title || '').toLowerCase().includes(q) ||
          String(m.teamName || '').toLowerCase().includes(q) ||
          String(m.email || '').toLowerCase().includes(q);
      })
      : allMembers;

    memberListEl.innerHTML = '';
    data.forEach(function (m) {
      var li = document.createElement('li');
      li.style.cssText = 'display:flex;align-items:center;gap:10px;padding:8px;border-radius:8px;';
      li.addEventListener('mouseenter', function () { li.style.background = '#f3f4f6'; });
      li.addEventListener('mouseleave', function () { li.style.background = 'transparent'; });

      var avatarHtml = (m && m.avatarUrl)
        ? '<img src="' + m.avatarUrl + '" alt="" style="width:100%;height:100%;object-fit:cover">'
        : String(m && m.name ? m.name : 'U').charAt(0);

      var subline = (m.title || '') + (m.teamName ? ' • ' + m.teamName : '') + (m.email ? ' • ' + m.email : '');
      var checked = selectedAttendees.has(String(m.id)) ? ' checked' : '';

      li.innerHTML =
        '<div style="width:34px;height:34px;border-radius:999px;overflow:hidden;background:#eef2ff;display:grid;place-items:center;flex:0 0 34px">' +
        avatarHtml +
        '</div>' +
        '<div style="flex:1;min-width:0">' +
        '<div style="font-weight:600">' + (m.name || '') + '</div>' +
        '<div style="font-size:12px;color:#6b7280;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">' + subline + '</div>' +
        '</div>' +
        '<label><input type="checkbox" class="attendee-check" data-id="' + m.id + '"' + checked + ' /></label>';

      memberListEl.appendChild(li);
    });

    var visibleIds = data.map(function (m) { return String(m.id); });
    var allVisibleChecked = visibleIds.length > 0 && visibleIds.every(function (id) { return selectedAttendees.has(id); });
    memberCheckAll.checked = allVisibleChecked;
  }

  memberListEl.addEventListener('change', function (e) {
    var box = e.target.closest('.attendee-check'); if (!box) return;
    var id = String(box.dataset.id);
    if (box.checked) selectedAttendees.add(id); else selectedAttendees.delete(id);
    renderMemberList(memberFilterEl.value);
  });
  memberFilterEl.addEventListener('input', debounce(function () { renderMemberList(memberFilterEl.value); }, 160));
  memberCheckAll.addEventListener('change', function () {
    var q = (memberFilterEl.value || '').trim().toLowerCase();
    var visible = (q
      ? allMembers.filter(function (m) {
        return String(m.name || '').toLowerCase().includes(q) ||
          String(m.title || '').toLowerCase().includes(q) ||
          String(m.teamName || '').toLowerCase().includes(q) ||
          String(m.email || '').toLowerCase().includes(q);
      })
      : allMembers).map(function (m) { return String(m.id); });

    if (memberCheckAll.checked) visible.forEach(function (id) { selectedAttendees.add(id); });
    else visible.forEach(function (id) { selectedAttendees.delete(id); });

    renderMemberList(memberFilterEl.value);
  });

  // FullCalendar (글로벌 번들: plugins 옵션 필요 없음)
  var calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: 'dayGridMonth',
    headerToolbar: { left: 'prev,next today', center: 'title', right: 'dayGridMonth,timeGridWeek,timeGridDay' },
    events: ctx + '/api/projects/' + projectId + '/calendar/events',
    locale: 'ko',
    selectable: true,
    selectMirror: true,
    editable: true,
    dayMaxEvents: true,
    displayEventTime: false,
    dateClick: function (info) { openModalForNewEvent(info.dateStr); },
    select: function (info) { var d = info.startStr.slice(0, 10); openModalForNewEvent(d); calendar.unselect(); },
    dayCellDidMount: function (arg) {
      var isoDate = arg.date.toISOString().slice(0, 10);
      var anchor = arg.el.querySelector('.fc-daygrid-day-number');
      if (anchor) anchor.addEventListener('click', function (e) { e.preventDefault(); openModalForNewEvent(isoDate); });
      arg.el.addEventListener('dblclick', function () { openModalForNewEvent(isoDate); });
    },
    eventClick: function (info) { openModalForExistingEvent(info.event); },
    dayCellContent: function (info) { return info.dayNumberText.replace('일', ''); }
  });

  calendar.render();
  closePopup();

  // 신규
  function openModalForNewEvent(date) {
    eventForm.reset();
    modalTitle.textContent = '일정 추가';
    btnDelete.style.display = 'none';
    document.getElementById('event-id').value = '';
    Array.prototype.forEach.call(daySelector.querySelectorAll('.day-btn.active'), function (b) { b.classList.remove('active'); });
    relatedTaskSelect.value = '';

    selectedAttendees.clear();
    memberFilterEl.value = '';
    renderMemberList('');

    startInput.type = 'datetime-local';
    endInput.type = 'datetime-local';
    startInput.value = date + 'T09:00';
    endInput.value = date + 'T10:00';

    openPopup();
  }

  // 기존
  function openModalForExistingEvent(event) {
    eventForm.reset();
    modalTitle.textContent = '일정 수정';
    btnDelete.style.display = String(event.id).startsWith('T-') ? 'none' : 'block';
    Array.prototype.forEach.call(daySelector.querySelectorAll('.day-btn.active'), function (b) { b.classList.remove('active'); });

    document.getElementById('event-id').value = event.id;
    document.getElementById('event-title').value = event.title;

    allDayCheckbox.checked = event.allDay;
    handleAllDayChange(event.allDay, event.start, event.end);

    if (event.extendedProps && event.extendedProps.rrule) {
      var weekdays = ['SU', 'MO', 'TU', 'WE', 'TH', 'FR', 'SA'];
      var part = event.extendedProps.rrule.split('BYDAY=');
      var byday = part.length > 1 ? part[1] : '';
      if (byday) {
        byday.split(',').forEach(function (d) {
          var idx = weekdays.indexOf(d);
          if (idx !== -1) {
            var btn = daySelector.querySelector('[data-day="' + idx + '"]');
            if (btn) btn.classList.add('active');
          }
        });
      }
    }

    document.getElementById('event-location').value = (event.extendedProps && event.extendedProps.location) || '';
    document.getElementById('event-memo').value = (event.extendedProps && event.extendedProps.memo) || '';
    document.getElementById('event-color').value = event.backgroundColor || 'blue';
    relatedTaskSelect.value = (event.extendedProps && event.extendedProps.relatedTaskId) || '';

    // 참석자 로드
    selectedAttendees.clear();
    Promise.resolve().then(function () {
      if (!String(event.id).startsWith('T-')) {
        return api.attendeesList(event.id).then(function (ids) {
          (ids || []).forEach(function (id) { selectedAttendees.add(String(id)); });
        });
      }
    }).catch(function () { }).finally(function () {
      memberFilterEl.value = '';
      renderMemberList('');
      openPopup();
    });
  }

  // 종일 토글
  allDayCheckbox.addEventListener('change', function () { handleAllDayChange(allDayCheckbox.checked); });
  function handleAllDayChange(isAllDay, startDate, endDate) {
    startDate = startDate || new Date(startInput.value || Date.now());
    endDate = endDate || new Date(endInput.value || startDate);
    if (isAllDay) {
      startInput.type = 'date'; endInput.type = 'date';
      startInput.value = toLocalDate(startDate);
      endInput.value = toLocalDate(endDate);
    } else {
      startInput.type = 'datetime-local'; endInput.type = 'datetime-local';
      startInput.value = toLocalDateTime(startDate);
      endInput.value = toLocalDateTime(endDate);
    }
  }

  // 요일 토글
  daySelector.addEventListener('click', function (ev) {
    if (ev.target.classList.contains('day-btn')) ev.target.classList.toggle('active');
  });

  // 저장
  btnSave.addEventListener('click', function (e) {
    e.preventDefault();

    if (!allDayCheckbox.checked) {
      var st = new Date(startInput.value), en = new Date(endInput.value);
      if (isFinite(st) && isFinite(en) && en <= st) {
        showToast('종료일은 시작일 이후여야 합니다.', 'error', { position: 'bottom-end' });
        return;
      }
    }

    var eventId = document.getElementById('event-id').value;
    var selectedDays = Array.prototype.map.call(daySelector.querySelectorAll('.day-btn.active'), function (b) { return b.dataset.day; });
    var weekdays = ['SU', 'MO', 'TU', 'WE', 'TH', 'FR', 'SA'];
    var bydayString = selectedDays.map(function (d) { return weekdays[d]; }).join(',');

    var payload = {
      id: eventId || null,
      title: document.getElementById('event-title').value,
      start: startInput.value,
      end: endInput.value,
      allDay: allDayCheckbox.checked,
      backgroundColor: document.getElementById('event-color').value,
      extendedProps: {
        location: document.getElementById('event-location').value,
        memo: document.getElementById('event-memo').value,
        relatedTaskId: relatedTaskSelect.value || null,
        rrule: bydayString ? ('FREQ=WEEKLY;BYDAY=' + bydayString) : null
      }
    };

    if (eventId && String(eventId).startsWith('T-')) {
      showToast('태스크 일정은 태스크 화면에서 수정하세요.', 'warning');
      closePopup();
      return;
    }

    var method = eventId ? 'PUT' : 'POST';
    var url = eventId
      ? (ctx + '/api/projects/' + projectId + '/calendar/events/' + eventId)
      : (ctx + '/api/projects/' + projectId + '/calendar/events');

    fetch(url, { method: method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
      .then(function (res) { if (!res.ok) throw new Error('save failed'); return res.json(); })
      .then(function (saved) {
        var savedId = saved && saved.id ? saved.id : eventId;
        if (savedId && !String(savedId).startsWith('T-')) {
          return api.attendeesReplace(savedId, Array.from(selectedAttendees).map(Number));
        }
      })
      .then(function () {
        calendar.refetchEvents();
        showToast(eventId ? '일정이 수정되었습니다.' : '일정이 등록되었습니다.', 'success', { duration: 2500, position: 'bottom-end' });
      })
      .catch(function () {
        showToast('처리에 실패했습니다. 다시 시도해주세요.', 'error', { position: 'bottom-end' });
      })
      .finally(function () { closePopup(); });
  });

  // 삭제
  btnDelete.addEventListener('click', function () {
    var eventId = document.getElementById('event-id').value;
    fetch(ctx + '/api/projects/' + projectId + '/calendar/events/' + eventId, { method: 'DELETE' })
      .then(function () {
        calendar.refetchEvents();
        showToast('일정이 삭제되었습니다.', 'warning', { duration: 2500, position: 'bottom-end' });
      })
      .finally(function () { closePopup(); });
  });

  // 팝업 닫기
  var popup = document.getElementById('event-popup');
  Array.prototype.forEach.call(document.querySelectorAll('.popup-close'), function (b) { b.addEventListener('click', closePopup); });
  popup.addEventListener('click', function (e) { if (e.target === popup) closePopup(); });

  // 토스트
  function showToast(message, type, opts) {
    message = message || '완료되었습니다.'; type = type || 'success'; opts = opts || {};
    var el = document.getElementById('planToast'); if (!el) return;
    var container = el.closest('.toast-container');
    var pos = opts.position || 'top-end';
    container.className = 'toast-container position-fixed p-3 ' +
      (pos.indexOf('bottom') >= 0 ? 'bottom-0' : 'top-0') + ' ' +
      (pos.indexOf('start') >= 0 ? 'start-0' : 'end-0');

    el.className = 'toast clean-toast';
    el.classList.add('toast-' + type);
    el.querySelector('.toast-body').textContent = message;
    el.querySelector('.toast-icon').textContent = ({ success: '✔', error: '✖', warning: '!', info: 'ℹ' })[type] || 'ℹ';
    var t = bootstrap.Toast.getOrCreateInstance(el, { autohide: true, delay: Number(opts.duration || 2200) });
    t.show();
  }
})();
