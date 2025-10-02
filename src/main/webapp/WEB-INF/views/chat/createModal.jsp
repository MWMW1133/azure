<%--<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>--%>

<%--<div id="createChatModal" class="modal-overlay">--%>
<%--    <div class="modal-content" style="width: 400px;">--%>
<%--        <!-- 헤더 -->--%>
<%--        <div class="modal-header d-flex align-items-center">--%>
<%--            <button type="button" class="close-btn me-2" onclick="closeCreateChatModal()">←</button>--%>
<%--            <h6 class="m-0 fw-bold">채팅방 생성</h6>--%>
<%--        </div>--%>

<%--        <!-- 본문 -->--%>
<%--        <div class="modal-body p-3">--%>
<%--            <div class="mb-2 fw-bold">목록</div>--%>
<%--            <div class="mb-3">--%>
<%--                <label class="form-label d-flex align-items-center gap-2 fw-bold">--%>
<%--                    <i class="bi bi-people"></i> 프로젝트--%>
<%--                </label>--%>
<%--                <select class="form-select">--%>
<%--                    <option>프로젝트 1</option>--%>
<%--                    <option>프로젝트 2</option>--%>
<%--                    <option>프로젝트 3</option>--%>
<%--                </select>--%>
<%--            </div>--%>
<%--            <p class="small text-muted">프로젝트 내부의 모든 구성원이 참여할 수 있습니다.</p>--%>

<%--            <!-- 버튼 -->--%>
<%--            <div class="d-flex justify-content-end gap-2 mt-3">--%>
<%--                <button class="btn btn-secondary" onclick="closeCreateChatModal()">취소</button>--%>
<%--                <button class="btn btn-success">생성하기</button>--%>
<%--            </div>--%>
<%--        </div>--%>
<%--    </div>--%>
<%--</div>--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<div id="createChatModal" class="modal-overlay">
    <div class="modal-content" style="width: 400px;">
        <!-- 헤더 -->
        <div class="modal-header d-flex align-items-center">
            <button type="button" class="close-btn me-2" onclick="closeCreateChatModal()">←</button>
            <h6 class="m-0 fw-bold">채팅방 생성</h6>
        </div>

        <!-- 본문 -->
        <div class="modal-body p-3">
            <div class="mb-2 fw-bold">목록</div>

            <!-- 프로젝트 선택 -->
            <div class="mb-3">
                <label class="form-label d-flex align-items-center gap-2 fw-bold">
                    <i class="bi bi-people"></i> 프로젝트
                </label>
                <!-- TODO: JS에서 프로젝트 옵션 주입 -->
                <select id="projectSelect" class="form-select"></select>
            </div>

            <p class="small text-muted">프로젝트 내부의 모든 구성원이 참여할 수 있습니다.</p>

            <!-- 버튼 -->
            <div class="d-flex justify-content-end gap-2 mt-3">
                <button class="btn btn-secondary" onclick="closeCreateChatModal()">취소</button>
                <button class="btn btn-success">생성하기</button>
            </div>
        </div>
    </div>
</div>
