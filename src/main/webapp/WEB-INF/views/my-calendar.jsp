<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div id="calendar-container">
  <div id="calendar"></div>
</div>

<!-- 팝업 -->
<div id="event-popup" class="event-popup" style="display: none">
  <div class="popup-content">
    <!-- 닫기 버튼 -->
    <button type="button" class="popup-close">&times;</button>

    <h5 id="modal-title">일정</h5>
    <form id="event-form">
      <input type="hidden" id="event-id" />

      <!-- 일정 제목 -->
      <div class="form-group">
        <label for="event-title">일정 제목</label>
        <input type="text" id="event-title" required />
      </div>

      <!-- 시작/종료일 -->
      <div class="form-row">
        <div class="form-group">
          <label for="event-start">시작일</label>
          <input type="datetime-local" id="event-start" />
        </div>
        <div class="form-group">
          <label for="event-end">종료일</label>
          <input type="datetime-local" id="event-end" />
        </div>
      </div>

      <!-- 종일 -->
      <div class="form-row form-row-opts">
        <div class="form-group-inline">
          <label for="event-all-day">종일</label>
          <input type="checkbox" id="event-all-day" />
        </div>
      </div>

      <!-- 반복 요일 -->
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

      <!-- 위치 -->
      <div class="form-group">
        <label for="event-location">위치</label>
        <input type="text" id="event-location" />
      </div>

      <!-- 메모 -->
      <div class="form-group">
        <label for="event-memo">일정 메모</label>
        <textarea id="event-memo" rows="3"></textarea>
      </div>

      <!-- 색상 -->
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

      <!-- 버튼 -->
      <div class="form-actions">
        <button type="button" class="btn-delete" id="btn-delete" style="display: none">삭제</button>
        <button type="submit" class="btn-save" id="btn-save">완료</button>
      </div>
    </form>
  </div>
</div>
<div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 2000">
  <div id="planToast" class="toast clean-toast" role="alert" aria-live="assertive" aria-atomic="true">
    <div class="d-flex align-items-center">
      <div class="toast-icon me-2" aria-hidden="true"></div>
      <div class="toast-body">메시지</div>
    </div>
  </div>
</div>
