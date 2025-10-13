<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<div class="chart-wrapper">
  <div class="task-count-container">
    <div class="task-count-header">
      <div class="task-count-title">전체 태스크</div>
    </div>
    <div class="task-count-body">
      <span id="total-task-count">${totalTaskCount}</span>
    </div>
  </div>
  <div class="task-count-container">
    <div class="task-count-header">
      <div class="task-count-title">진행중인 태스크</div>
    </div>
    <div class="task-count-body">
      <span id="active-task-count">${activeTaskCount}</span>
    </div>
  </div>
  <div class="task-count-container">
    <div class="task-count-header">
      <div class="task-count-title">완료된 태스크</div>
    </div>
    <div class="task-count-body">
      <span id="archived-task-count">${archivedTaskCount}</span>
    </div>
  </div>
  <div class="chart-graph-wrapper" id="graphByAssignee" data-title="직원 별 담당 태스크"></div>
  <div class="chart-graph-wrapper" id="graphByDueDate"  data-title="태스크 마감일"></div>
  <div class="chart-graph-wrapper" id="graphByWorkflow" data-title="상태 별 태스크"></div>
</div>
