<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- 뷰어 오버레이(처음엔 숨김) -->
<div class="pplan-modal-overlay" id="pplan-viewer-overlay" style="display: none">
  <div class="pplan-viewer-container" id="pplan-viewer-container">
    <div class="pplan-viewer-header">
      <div class="pplan-viewer-title"><span id="pv-title">-</span> 계획서</div>
      <button type="button" class="popup-close" data-close="viewer">&times;</button>
    </div>

    <div class="pplan-viewer-body">
      <div class="form-row">
        <div class="form-group-inline">
          <label>작성자</label>
          <div class="read-only-field" id="pv-proposer">-</div>
        </div>
        <div class="form-group-inline">
          <label>예상 기간</label>
          <div class="read-only-field" id="pv-duration">-</div>
        </div>
      </div>

      <div class="form-row">
        <div class="form-group-inline">
          <label>생성일</label>
          <div class="read-only-field" id="pv-created">-</div>
        </div>
        <div class="form-group-inline">
          <label>상태</label>
          <div class="read-only-field" id="pv-status-field">
            <span class="pplan-status" id="pv-status-badge">-</span>
          </div>
        </div>
      </div>

      <div class="form-group">
        <label>프로젝트 설명</label>
        <div class="read-only-field content-box" id="pv-description">-</div>
      </div>
    </div>

    <div class="pplan-viewer-footer">
      <div class="form-actions">
        <!-- 권한 체크 필요-->
        <button type="button" id="btn-approved" class="btn-approved">프로젝트 승인</button>
        <button type="button" id="btn-rejected" class="btn-rejected">프로젝트 거절</button>
      </div>
    </div>
  </div>
</div>
