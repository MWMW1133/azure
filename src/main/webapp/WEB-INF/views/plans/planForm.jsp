<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="pplan-modal-overlay" id="pplan-modal-overlay" style="display: none">
  <div class="pplan-form-container">
    <div class="pplan-form-header">
      <div class="pplan-form-title">프로젝트 계획 추가</div>
      <button type="button" class="popup-close">&times;</button>
    </div>
    <div class="pplan-template-body">
      <form id="project-pplan-form">
        <input type="hidden" id="pplan-id" />
        <div class="form-row">
          <div class="form-group">
            <label for="pplan-title">프로젝트 명</label>
            <input type="text" id="pplan-title" required />
          </div>
          <div class="form-group">
            <label for="pplan-duration">기간 설정</label>
            <input type="text" id="pplan-duration" class="form-input" placeholder="YYYY/MM/DD ~ YYYY/MM/DD" />
          </div>
        </div>

        <div class="form-group">
          <label for="pplan-description">프로젝트 내용</label>
          <textarea id="pplan-description" rows="8"></textarea>
        </div>

        <div class="form-actions">
          <button type="submit" id="btn-save" class="btn-save">+ 제출하기</button>
        </div>
      </form>
    </div>
  </div>
</div>
