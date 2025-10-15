<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- FullCalendar & Bootstrap (순서 중요) -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.15/index.global.min.css">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"/>

<script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.15/index.global.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/rrule@2.7.2/dist/es5/rrule.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/@fullcalendar/rrule@6.1.15/index.global.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

<style>
  #calendar{min-height:620px}
  .event-popup{position:fixed;inset:0;background:rgba(0,0,0,.35);display:none;align-items:center;justify-content:center}
  .event-popup.is-open{display:flex}
  .popup-content{background:#fff;border-radius:12px;max-width:680px;width:92vw;padding:16px;position:relative}
  .popup-close{position:absolute;right:12px;top:8px;border:none;background:transparent;font-size:22px;cursor:pointer}
  .form-row{display:flex;gap:12px}
  .form-group{margin-bottom:10px;flex:1}
  .form-group-inline{display:flex;align-items:center;gap:8px}
  .day-selector-group{display:flex;gap:6px;flex-wrap:wrap}
  .day-btn{border:1px solid #e5e7eb;background:#f8f9fa;border-radius:6px;padding:6px 10px;cursor:pointer}
  .day-btn.active{background:#0d6efd;color:#fff;border-color:#0d6efd}
  .form-actions{display:flex;justify-content:space-between;gap:8px;margin-top:8px}
  .btn-save,.btn-delete{padding:8px 12px;border-radius:6px;border:none;cursor:pointer}
  .btn-save{background:#0d6efd;color:#fff}
  .btn-delete{background:#dc3545;color:#fff}
  .clean-toast{min-width:260px}
  .toast-icon{width:22px;text-align:center}
</style>

<div id="calendar-container"
     data-project-id="${param.projectId != null ? param.projectId : projectId}"
     data-ctx="${pageContext.request.contextPath}">
  <!-- 캘린더 -->
  <div id="calendar"></div>

  <!-- 팝업 -->
  <div id="event-popup" class="event-popup">
    <div class="popup-content">
      <button type="button" class="popup-close" aria-label="닫기">&times;</button>
      <h5 id="modal-title">일정</h5>

      <form id="event-form" autocomplete="off">
        <input type="hidden" id="event-id"/>

        <div class="form-group">
          <label for="event-title">일정 제목</label>
          <input type="text" id="event-title" required/>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="event-start">시작일</label>
            <input type="datetime-local" id="event-start"/>
          </div>
          <div class="form-group">
            <label for="event-end">종료일</label>
            <input type="datetime-local" id="event-end"/>
          </div>
        </div>

        <div class="form-row form-row-opts">
          <div class="form-group-inline">
            <label for="event-all-day">종일</label>
            <input type="checkbox" id="event-all-day"/>
          </div>
          <div class="form-group-inline" style="gap:6px">
            <label for="event-related-task">관련 태스크</label>
            <select id="event-related-task">
              <option value="">선택 안 함</option>
            </select>
          </div>
        </div>

        <!-- 참석자 -->
        <div class="form-group">
          <label>참석자</label>
          <div class="attendee-picker" style="border:1px solid #e5e7eb;border-radius:12px;padding:12px;">
            <div class="attendee-toolbar" style="display:flex;gap:8px;align-items:center;margin-bottom:8px;">
              <input id="member-filter" placeholder="이름, 직위, 또는 팀을 검색하세요."
                     style="flex:1;border:1px solid #e5e7eb;border-radius:8px;padding:8px 10px" />
              <label style="display:flex;align-items:center;gap:6px;font-size:.92rem;color:#374151">
                <input type="checkbox" id="member-check-all"/> 전체선택
              </label>
            </div>
            <ul id="member-list"
                style="list-style:none;margin:0;padding:4px;max-height:260px;overflow:auto;border-radius:8px;background:#fafafa">
              <!-- JS에서 렌더링 -->
            </ul>
            <div style="margin-top:6px;color:#6b7280;font-size:.85rem">
              해당 프로젝트에 참여중인 인원만 표시
            </div>
          </div>
        </div>

        <div class="form-group">
          <label>반복 요일</label>
          <div class="day-selector-group">
            <button type="button" class="day-btn" data-day="1">월</button>
            <button type="button" class="day-btn" data-day="2">화</button>
            <button type="button" class="day-btn" data-day="3">수</button>
            <button type="button" class="day-btn" data-day="4">목</button>
            <button type="button" class="day-btn" data-day="5">금</button>
            <button type="button" class="day-btn" data-day="6">토</button>
            <button type="button" class="day-btn" data-day="0">일</button>
          </div>
        </div>

        <div class="form-group">
          <label for="event-location">위치</label>
          <input type="text" id="event-location"/>
        </div>

        <div class="form-group">
          <label for="event-memo">일정 메모</label>
          <textarea id="event-memo" rows="3"></textarea>
        </div>

        <div class="form-group">
          <label for="event-color">일정 색상</label>
          <select id="event-color">
            <option value="red">빨간색</option>
            <option value="yellow">노란색</option>
            <option value="orange">주황색</option>
            <option value="green">초록색</option>
            <option value="blue" selected>파란색</option>
          </select>
        </div>

        <div class="form-actions">
          <button type="button" class="btn-delete" id="btn-delete" style="display:none">삭제</button>
          <button type="submit" class="btn-save" id="btn-save">완료</button>
        </div>
      </form>
    </div>
  </div>

  <!-- Toast -->
  <div class="toast-container position-fixed top-0 end-0 p-3" style="z-index:2000">
    <div id="planToast" class="toast clean-toast" role="alert" aria-live="assertive" aria-atomic="true">
      <div class="d-flex align-items-center">
        <div class="toast-icon me-2" aria-hidden="true"></div>
        <div class="toast-body">메시지</div>
      </div>
    </div>
  </div>
</div>

<!-- 우리 JS (FullCalendar/rrule 로드 이후) -->
<script src="${pageContext.request.contextPath}/js/projects/project-calendar.js"></script>
