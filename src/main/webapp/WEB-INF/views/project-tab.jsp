<%@ page contentType="text/html; charset=UTF-8" %>
<link rel="stylesheet"
      href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" />

<link href="/css/project-tab.css" rel="stylesheet">
<link href="/css/projects/mainTable.css" rel="stylesheet">
<link href="/css/taskRow.css" rel="stylesheet">
<link href="/css/projects/taskForm.css" rel="stylesheet">
<link rel="stylesheet" href="https://unpkg.com/frappe-gantt/dist/frappe-gantt.css" />
<script src="https://unpkg.com/frappe-gantt/dist/frappe-gantt.umd.js"></script>

<link href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.css" rel="stylesheet">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">
<script src="${pageContext.request.contextPath}/js/bootstrap/bootstrap.bundle.js"></script>

<div class="project-wrapper"
     id="project-tab-root"
     data-project-id="${projectId}"
     data-project-name="${projectName}"
     data-context-path="${pageContext.request.contextPath}">
  <div class="project-header">
    <div class="project-header-left">
      <div class="project-title"></div>
      <div class="project-tags"></div>
    </div>
    <div class="project-header-right">
      <button id="addTagBtn"
              type="button"
              class="project-header-btn btn-primary"
              data-bs-toggle="popover"
              data-bs-placement="bottom"
              data-bs-container="body"
              data-bs-custom-class="glass-popover"    
              data-bs-title="태그 추가"     
              data-bs-html="true"
              data-bs-sanitize="false"
              data-bs-content='
                <div class="p-2">
                  <input class="form-control" placeholder="태그를 입력하세요." />
                </div>'>
        <i class="bi bi-tag"></i>태그 추가
      </button>

      <button class="project-header-btn btn-secondary" id="project-add-member">
        <i class="bi bi-person-plus"></i>
        초대
      </button>
    <!--프로젝트 초대 누르면 나올 panel-->
    <div id="invite-panel" class="invite-panel" role="dialog" aria-modal="true" aria-labelledby="invite-title">
      <div class="invite-header">
        <h3 id="invite-title"><span id="invite-project-name"></span> 초대</h3>
      </div>
      <div class="invite-search">
        <i class="bi bi-search"></i>
        <input id="invite-search-input" type="text" placeholder="검색" aria-label="사용자 검색">
      </div>
      <ul id="invite-list" class="invite-list" aria-label="사용자 목록">
        <!-- js에서 사용자 목록 불러들이기 -->
      </ul>
      <div class="invite-footer">
        <button id="invite-submit" class="invite-btn" disabled>
          <i class="bi bi-plus-lg"></i> 초대하기
        </button>
      </div>
    </div>
    </div>
  </div>
  <div class="project-toolbar">
  <nav class="nav-project">
    <div class="project-tabs" role="tablist" aria-label="Project views">
      <button class="project-tab is-active" role="tab" aria-selected="true" data-view="table"><span>메인 테이블</span></button>
      <button class="project-tab" role="tab" aria-selected="false" data-view="gantt"><span>간트</span></button>
      <button class="project-tab" role="tab" aria-selected="false" data-view="chart"><span>차트</span></button>
      <button class="project-tab" role="tab" aria-selected="false" data-view="calendar"><span>캘린더</span></button>
      <button class="project-tab" role="tab" aria-selected="false" data-view="files"><span>파일</span></button>
      <button class="project-tab" role="tab" aria-selected="false" data-view="members"><span>멤버</span></button>
    </div>
  </nav>
  </div>
  <div class="project-body"></div>
</div>
<!-- 초대 알림 -->
<div id="inviteToastContainer"
     class="toast-container position-fixed end-0 p-3"
     aria-live="polite" aria-atomic="true">
  <div id="inviteToast" class="toast align-items-center text-white border-0" role="status">
    <div class="d-flex">
      <div class="toast-body"></div>
      <button type="button" class="btn-close btn-close-white me-2 m-auto"
              data-bs-dismiss="toast" aria-label="Close"></button>
    </div>
  </div>
</div>



<script src="/js/project-tab.js"></script>
<script src="/js/projects/mainTable.js"></script>
<script src="/js/projects/taskRow.js"></script>
<script src="/js/projects/ganttTab.js"></script>
<script src="https://cdn.jsdelivr.net/npm/echarts@5/dist/echarts.min.js"></script>
<script src="/js/projects/chartTab.js"></script>