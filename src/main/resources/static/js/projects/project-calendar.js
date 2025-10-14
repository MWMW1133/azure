/* /js/projects/project-calendar.js */
(function () {
  let inited = false;
  const $id = (s) => document.getElementById(s);

  function actuallyInit() {
    const container = document.getElementById('calendar-container');
    if (!container) return;

    const ctx = (container.dataset.ctx || '').replace(/\/$/, '');
    const projectId = container.dataset.projectId;

    const popup = $id('event-popup');
    const form = $id('event-form');

    const inpId = $id('event-id');
    const inpTitle = $id('event-title');
    const inpStart = $id('event-start');
    const inpEnd = $id('event-end');
    const chkAllDay = $id('event-all-day');
    const selTask = $id('event-related-task');
    const inpLocation = $id('event-location');
    const txtMemo = $id('event-memo');
    const selColor = $id('event-color');

    const memberFilter = $id('member-filter');
    const memberList = $id('member-list');
    const memberCheckAll = $id('member-check-all');

    const btnSave = $id('btn-save');
    const btnDelete = $id('btn-delete');

    const API = {
      members: `${ctx}/api/projects/${projectId}/members`,
      tasks: `${ctx}/api/projects/${projectId}/tasks/brief`,
      events: `${ctx}/api/projects/${projectId}/calendar/events`,
      eventOne: (id) => `${ctx}/api/projects/${projectId}/calendar/events/${id}`,
      create: `${ctx}/api/projects/${projectId}/calendar/events`,
    };

    const toastEl = document.getElementById('planToast');
    const toastMsgEl = toastEl?.querySelector('.toast-body');
    let toast;
    function showToast(msg, ok = true) {
      try {
        toastMsgEl.textContent = msg;
        toastEl.classList.toggle('toast-error', !ok);
        toastEl.classList.toggle('toast-success', ok);
        if (!toast) toast = new bootstrap.Toast(toastEl, { delay: 1600 });
        toast.show();
      } catch { console.log(msg); }
    }

    function openPopup() { popup?.classList.add('is-open'); }
    function closePopup() {
      if (!popup) return;
      popup.classList.remove('is-open');
      form?.reset?.();
      inpId.value = '';
      memberCheckAll && (memberCheckAll.checked = false);
      if (memberList) renderMemberChecks(cachedMembers, memberFilter?.value || '');
    }
    popup?.querySelector('.popup-close')?.addEventListener('click', closePopup);
    popup?.addEventListener('click', (e) => { if (e.target === popup) closePopup(); });

    let calendar;

    (async function boot() {
      await Promise.all([loadMembers(), loadTasks()]);
      const calEl = document.getElementById('calendar');
      calendar = new FullCalendar.Calendar(calEl, {
        themeSystem: 'standard',
        timeZone: 'local',
        initialView: 'dayGridMonth',
        height: 'auto',
        headerToolbar: { left: 'prev,next today', center: 'title', right: 'dayGridMonth,timeGridWeek,timeGridDay' },
        selectable: false,
        nowIndicator: true,
        eventOverlap: true,
        eventSources: [{
          url: API.events,
          method: 'GET',
          failure: () => showToast('일정 로드 실패', false),
          extraParams: () => ({ _: Date.now() }),
        }],
        dateClick: makeDoubleClickHandler((arg) => {
          const start = new Date(arg.date);
          const end = new Date(start.getTime() + 60 * 60 * 1000);
          fillForm({ start, end, allDay: false });
          openPopup();
        }),
        eventClick: async (info) => { await openForEdit(info.event); },
        editable: true,
        eventDrop: onEventDropResize,
        eventResize: onEventDropResize,
        locale: 'ko',
      });

      calendar.render();
      hookCalendarResizeFix();
    })();

    function makeDoubleClickHandler(cb, gap = 300) {
      let last = 0;
      return function (arg) {
        const now = Date.now();
        if (now - last < gap) cb(arg);
        last = now;
      };
    }

    async function onEventDropResize(info) {
      try {
        const body = {
          title: info.event.title,
          start: info.event.start?.toISOString(),
          end: info.event.end?.toISOString(),
          allDay: !!info.event.allDay,
          backgroundColor: info.event.backgroundColor || info.event.extendedProps.color,
          location: info.event.extendedProps.location || '',
          description: info.event.extendedProps.description || '',
          rrule: info.event.extendedProps.rrule || undefined,
          extendedProps: {
            relatedTaskId: info.event.extendedProps.related_task_id || info.event.extendedProps.relatedTaskId || null
          }
        };
        const res = await fetch(API.eventOne(info.event.id), {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(body)
        });
        if (!res.ok) throw new Error();
        showToast('일정이 업데이트됐습니다');
      } catch (e) {
        info.revert();
        showToast('일정 이동/수정 실패', false);
      }
    }

    // ===== 멤버 렌더링 =====
    let cachedMembers = [];
    async function loadMembers() {
      try {
        const res = await fetch(API.members);
        if (!res.ok) throw new Error();
        cachedMembers = await res.json();
        renderMemberChecks(cachedMembers, '');
        memberFilter?.addEventListener('input', () => {
          renderMemberChecks(cachedMembers, memberFilter.value);
        });
        memberCheckAll?.addEventListener('change', () => {
          memberList?.querySelectorAll('input[type="checkbox"]').forEach(ch => {
            ch.checked = memberCheckAll.checked;
          });
        });
      } catch {
        cachedMembers = [];
        renderMemberChecks([], '');
      }
    }

    function renderMemberChecks(members, keyword) {
      if (!memberList) return;
      const kw = (keyword || '').trim().toLowerCase();
      const filtered = kw
        ? members.filter(m =>
            (m.name || '').toLowerCase().includes(kw) ||
            (m.position || '').toLowerCase().includes(kw) ||
            (m.team || '').toLowerCase().includes(kw))
        : members;

      memberList.innerHTML = '';
      if (!filtered.length) {
        memberList.innerHTML = `<li style="padding:8px 10px;color:#9ca3af">검색 결과가 없습니다</li>`;
        return;
      }
      const frag = document.createDocumentFragment();
      filtered.forEach(m => {
        const li = document.createElement('li');
        li.style.cssText = 'display:flex;align-items:center;gap:10px;padding:8px 10px;border-bottom:1px solid #eee';
        li.innerHTML = `
          <input type="checkbox" class="att-chk" data-id="${m.id}">
          <div style="flex:1;display:flex;align-items:center;gap:10px">
            <div style="width:28px;height:28px;border-radius:50%;background:#e5e7eb;overflow:hidden"></div>
            <div>
              <div style="font-weight:600">${escapeHtml(m.name)}</div>
              <div style="font-size:.82rem;color:#6b7280">${escapeHtml(m.team || '')}${m.team ? ' · ' : ''}${escapeHtml(m.position || '')}</div>
            </div>
          </div>
        `;
        frag.appendChild(li);
      });
      memberList.appendChild(frag);
    }

    // ===== 태스크 로딩 =====
    async function loadTasks() {
      try {
        const res = await fetch(API.tasks);
        if (!res.ok) throw new Error();
        const tasks = await res.json();
        selTask.innerHTML = `<option value="">선택 안 함</option>` +
          tasks.map(t => `<option value="${t.id}">${escapeHtml(t.title)}</option>`).join('');
      } catch {
        // 404 등 실패 시 조용히 기본 옵션만
        selTask.innerHTML = `<option value="">선택 안 함</option>`;
      }
    }

    // ===== 폼 채우기/추출 =====
    function fillForm({ id, title, start, end, allDay, color, description, location, related_task_id, relatedTaskId, attendees }) {
      inpId.value = id || '';
      inpTitle.value = title || '';
      chkAllDay.checked = !!allDay;
      if (start) inpStart.value = toLocalInput(start);
      if (end)   inpEnd.value = toLocalInput(end);
      selColor.value = color || 'blue';
      txtMemo.value = description || '';
      inpLocation.value = location || '';
      selTask.value = related_task_id || relatedTaskId || '';
      const ids = new Set((attendees || []).map(a => a.id || a));
      memberList?.querySelectorAll('input.att-chk').forEach(ch => {
        ch.checked = ids.has(Number(ch.dataset.id));
      });
      btnDelete.style.display = id ? '' : 'none';
    }

    function extractForm() {
      const attendeeIds = Array.from(memberList?.querySelectorAll('input.att-chk:checked') || [])
        .map(ch => Number(ch.dataset.id));
      const start = inpStart.value ? new Date(inpStart.value) : null;
      const end = inpEnd.value ? new Date(inpEnd.value) : null;
      return {
        title: inpTitle.value?.trim(),
        start: start ? start.toISOString() : null,
        end: end ? end.toISOString() : null,
        allDay: chkAllDay.checked,
        backgroundColor: selColor.value,
        description: txtMemo.value?.trim() || '',
        location: inpLocation.value?.trim() || '',
        extendedProps: { relatedTaskId: selTask.value || null },
        attendeeIds // ← camelCase 유지
      };
    }

    function toLocalInput(dt) {
      const d = (dt instanceof Date) ? dt : new Date(dt);
      const pad = (n) => String(n).padStart(2, '0');
      return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
    }
    function escapeHtml(s) {
      return String(s || '').replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]));
    }

    // 단일 조회 → 폼 채우기
    async function openForEdit(eventObj) {
      try {
        const res = await fetch(API.eventOne(eventObj.id));
        const data = res.ok ? await res.json() : {
          id: eventObj.id,
          title: eventObj.title,
          start_at: eventObj.start?.toISOString(),
          end_at: eventObj.end?.toISOString(),
          allDay: eventObj.allDay,
          color: eventObj.backgroundColor || eventObj.extendedProps.color,
          description: eventObj.extendedProps.description,
          location: eventObj.extendedProps.location,
          related_task_id: eventObj.extendedProps.related_task_id,
          attendees: (eventObj.extendedProps.attendeeIds || []),
        };
        fillForm({
          id: data.id,
          title: data.title,
          start: data.start || data.start_at,
          end: data.end || data.end_at,
          allDay: data.allDay ?? data.all_day,
          color: data.backgroundColor || data.color,
          description: data.description,
          location: data.location,
          relatedTaskId: data.relatedTaskId,
          attendees: data.attendeeIds || data.attendees, // ← attendeeIds 우선
        });
        openPopup();
      } catch {
        showToast('일정 정보를 불러오지 못했습니다', false);
      }
    }

    // 저장/삭제
    form?.addEventListener('submit', async function (e) {
      e.preventDefault();
      try {
        const payload = extractForm();
        if (!payload.title) {
          inpTitle.focus();
          return showToast('제목을 입력해 주세요', false);
        }
        const isEdit = !!inpId.value;
        const url = isEdit ? API.eventOne(inpId.value) : API.create;
        const method = isEdit ? 'PUT' : 'POST';
        const res = await fetch(url, {
          method,
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        if (!res.ok) throw new Error();

        const saved = isEdit ? { id: inpId.value } : await res.json();

        // 참석자 저장 (별도 엔드포인트)
        const attendeeIds = payload.attendeeIds || [];  // ← camelCase로 고정
        await fetch(API.eventOne(saved.id) + '/attendees', {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ attendeeIds })
        });

        showToast(isEdit ? '일정을 수정했습니다' : '일정을 추가했습니다');
        closePopup();
        calendar.refetchEvents();
      } catch {
        showToast('저장에 실패했습니다', false);
      }
    });

    btnDelete?.addEventListener('click', async function () {
      if (!inpId.value) return;
      if (!confirm('이 일정을 삭제할까요?')) return;
      try {
        const res = await fetch(API.eventOne(inpId.value), { method: 'DELETE' });
        if (!res.ok) throw new Error();
        showToast('삭제되었습니다');
        closePopup();
        calendar.refetchEvents();
      } catch {
        showToast('삭제 실패', false);
      }
    });

    function hookCalendarResizeFix() {
      function ensureCalendarSized() {
        if (calendar) { try { calendar.updateSize(); } catch (_) {} }
      }
      window.addEventListener('resize', ensureCalendarSized);
      document.addEventListener('shown.bs.tab', (e) => {
        const target = e.target?.getAttribute('href') || e.target?.getAttribute('data-bs-target') || '';
        if (target.includes('calendar')) ensureCalendarSized();
      });
      document.addEventListener('click', (e) => {
        const tabBtn = e.target.closest('[data-tab="calendar"], .tab-calendar, a[href*="calendar"]');
        if (tabBtn) setTimeout(ensureCalendarSized, 0);
      });
      try {
        const io = new IntersectionObserver((entries) => {
          entries.forEach((ent) => { if (ent.isIntersecting) ensureCalendarSized(); });
        }, { root: null, threshold: 0.01 });
        io.observe(document.getElementById('calendar'));
      } catch (_) {}
    }
  }

  window.initProjectCalendar = function () {
    if (inited) return;
    inited = true;
    actuallyInit();
  };

  if (document.readyState !== 'loading') {
    if (document.getElementById('calendar-container')) window.initProjectCalendar();
  } else {
    document.addEventListener('DOMContentLoaded', () => {
      if (document.getElementById('calendar-container')) window.initProjectCalendar();
    });
  }
})();
