<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="d-flex">
    <div class="flex-grow-1 p-4">
        <div class="card p-4 position-relative">

            <!-- 주석 처리된 내용은 백엔드랑 연결하는 부분임!!!!!!!!!!!(나중에 더미 삭제하고 주석 해제할 것)-->
            <!-- 상단 우측 Save / Cancel -->
            <div class="position-absolute top-0 end-0 mt-3 me-3">
                <button class="btn btn-primary btn-sm">Save</button>
                <button class="btn btn-outline-secondary btn-sm">Cancel</button>
            </div>

            <!-- 프로필 영역 (사진 + 이름, 사진 업로드 아이콘 포함) -->
            <div class="d-flex align-items-center mb-4">
                <div class="position-relative">
                    <img src="${pageContext.request.contextPath}/images/my-cat.png"
                         alt="Profile" class="rounded-circle" width="96" height="96">
<%--                    <img src="${user.avatarUrl}" alt="Profile" class="rounded-circle" width="96" height="96">--%>
                    <!-- 업로드 아이콘 -->
                    <label for="profileImage" class="position-absolute bottom-0 end-0 bg-white rounded-circle p-1 shadow-sm"
                           style="cursor:pointer;">
                        <i class="bi bi-camera-fill"></i>
                    </label>
                    <input type="file" id="profileImage" class="d-none">
                </div>
                <h5 class="mb-0 ms-3">박소현</h5>
<%--                <h5 class="mb-0 ms-3">${user.name}</h5>--%>

            </div>
            <hr>

            <!-- 상세 속성 -->
            <div class="row mb-3">
                <!-- Full Name (수정 가능) -->
                <div class="col-md-6">
                    <label class="form-label small text-muted">Full Name</label>
                    <input type="text" class="form-control" value="박소현">
<%--                    <input type="text" class="form-control" value="${user.name}">--%>
                </div>
                <!-- 회사명 (수정 불가) -->
                <div class="col-md-6">
                    <label class="form-label small text-muted">회사명</label>
                    <input type="text" class="form-control bg-light text-muted" value="동의대학교 미래교육원" readonly>
<%--                    <input type="text" class="form-control bg-light text-muted" value="${org.name}" readonly>--%>
                </div>
            </div>

            <div class="row mb-3">
                <!-- 가입일 (수정 불가) -->
                <div class="col-md-6">
                    <label class="form-label small text-muted">가입일</label>
                    <input type="text" class="form-control bg-light text-muted" value="2025.09.25" readonly>
<%--                    <input type="text" class="form-control bg-light text-muted"--%>
<%--                           value="${user.createdAt.toLocalDate()}" readonly>--%>

                    <!-- 백엔드에서 포맷팅안할 경우, 프론트단에서 직접 처리 -->
<%--                    <input type="text" class="form-control bg-light text-muted"--%>
<%--                           value="<fmt:formatDate value='${user.createdAt}' pattern='yyyy.MM.dd'/>" readonly>--%>
                </div>
                <!-- ID (수정 불가) -->
                <div class="col-md-6">
                    <label class="form-label small text-muted">ID</label>
                    <input type="text" class="form-control bg-light text-muted" value="kingoreu" readonly>
<%--                    <input type="text" class="form-control bg-light text-muted" value="${user.id}" readonly>--%>
                </div>
            </div>

            <div class="row mb-3">
                <!-- 권한 (수정 불가) -->
                <div class="col-md-6">
                    <label class="form-label small text-muted">권한</label>
                    <input type="text" class="form-control bg-light text-muted" value="구성원" readonly>
<%--                    <input type="text" class="form-control bg-light text-muted" value="${org.role}" readonly>--%>
                </div>
                <!-- 근태 (수정 가능, select) -->
                <div class="col-md-6">
                    <label class="form-label small text-muted">활동 상태</label>
                    <select class="form-select">
                        <option selected>근무중</option>
                        <option>휴가</option>
<%--                        <option value="근무중" ${user.isActive ? "selected" : ""}>근무중</option>--%>
<%--                        <option value="휴가" ${!user.isActive ? "selected" : ""}>휴가</option>--%>
                    </select>
                </div>
            </div>

            <!-- 비밀번호 변경 버튼 -->
            <div class="mb-3">
                <label class="form-label small text-muted">비밀번호 변경</label><br>
                <button class="btn btn-outline-secondary btn-sm" data-bs-toggle="modal" data-bs-target="#changePwdModal">
                    Change Password
                </button>
            </div>

        </div>
    </div>
</div>

<!-- 비밀번호 변경 모달-->
<div class="modal fade" id="changePwdModal" tabindex="-1" aria-labelledby="changePwdLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog centered">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="changePwdModalLabel">비밀번호 변경</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <div class="modal-body">
                <form>
                    <div class="mb-3">
                        <label class="form-label">현재 비밀번호</label>
                        <input type="password" class="form-control"/>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">현재 비밀번호 확인</label>
                        <input type="password" class="form-control"/>
                        <div class="form-text text-success">비밀번호가 일치합니다.</div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">새 비밀번호</label>
                        <input type="password" class="form-control"/>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">새 비밀번호 확인</label>
                        <input type="password" class="form-control"/>
                    </div>
                </form>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-dark">저장</button>
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">취소</button>
            </div>
        </div>
    </div>
</div>