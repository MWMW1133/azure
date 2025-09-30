<%@ page contentType="text/html; charset=UTF-8" %>
<div class="project-wrapper">
  <div class="project-header">
    <div class="project-header-left">
      <div class="project-title"></div>
      <div class="project-tags"></div>
    </div>
    <div class="project-header-right">
      <button class="project-header-btn" id="project-add-tag">태그 추가</button>
      <button class="project-header-btn" id="project-add-member">프로젝트 추가</button>
    </div>
  </div>
  <div class="project-toolbar">
    <div class="project-tabs" role="tablist" aria-label="Project views">
      <button class="project-tab is-active" role="tab" aria-selected="true" data-view="table">메인 테이블</button>
      <button class="project-tab" role="tab" aria-selected="false" data-view="card">카드</button>
      <button class="project-tab" role="tab" aria-selected="false" data-view="gantt">간트</button>
      <button class="project-tab" role="tab" aria-selected="false" data-view="chart">차트</button>
      <button class="project-tab" role="tab" aria-selected="false" data-view="calendar">캘린더</button>
      <button class="project-tab" role="tab" aria-selected="false" data-view="files">파일</button>
    </div>
  </div>
  <div class="project-body"></div>
</div>
