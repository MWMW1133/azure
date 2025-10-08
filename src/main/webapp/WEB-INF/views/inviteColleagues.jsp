<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<meta charset="utf-8" />
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<style>
    #inviteModal .list-group {
        margin-bottom: 10px;
    }

    /* 섹션 제목 */
    #inviteModal h6.invite-section-title {
        margin-top: 15px;
        margin-bottom: 10px;
        font-weight: 600;
        color: #444;
    }

    /* 각 항목 */
    #inviteModal .list-group-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 10px 14px;
        border-radius: 10px;
        margin-bottom: 6px;
        background: #f9fafc;
        transition: background-color 0.15s ease;
    }

    #inviteModal .list-group-item:hover {
        background-color: #eef4ff;
    }

    /* 아바타 */
    #inviteModal img {
        border: 1px solid #dcdcdc;
        background: #fff;
        margin-right: 10px;
        vertical-align: middle;
    }

    /* 이름 + ID 정렬 */
    #inviteModal .user-info {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
    }

    /* 이름 */
    #inviteModal .user-info span {
        font-weight: 500;
        color: #333;
        margin-right: 6px;
    }

    /* ID (작게, 회색) */
    #inviteModal .user-info small {
        color: #777;
    }

    /* MANAGER 뱃지 */
    #inviteModal .badge {
        font-size: 0.75rem;
        padding: 4px 10px;
        border-radius: 12px;
        background-color: #4a68ff;   /* 파란색 강조 */
        color: #fff;                 /* 하얀 글씨 */
        font-weight: 600;
        letter-spacing: 0.3px;
    }

</style>

<div class="modal fade" id="inviteModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">새로운 구성원 초대하기</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <div class="modal-body">
                <!-- [1] 검색 영역 -->
                <!-- 신규 초대 대상 -->
                <h6 class="invite-section-title">사용자 검색</h6>
                <input type="text" id="inviteSearch" class="form-control mb-3"
                       placeholder="이름 또는 ID를 입력하세요.">
                <div id="inviteList" class="list-group mb-4">
                    <%-- 검색 결과가 여기에 fetch로 채워짐 --%>
                </div>


                <!-- [2] 현재 회사 멤버 목록 -->
                <h6 class="mb-2 fw-bold">현재 구성원</h6>
                <div class="list-group mb-4">
                    <c:forEach var="m" items="${orgMembers}">
                        <div class="list-group-item">
                            <div class="user-info">
                                <img src="${pageContext.request.contextPath}${m.user.avatarUrl}"
                                    class="rounded-circle me-2" width="32" height="32">
                                <span>${m.user.name}</span>
                                <small class="text-muted ms-2">(${m.user.loginId})</small>
                            </div>
                            <span class="badge">${m.role}</span>
                        </div>
                    </c:forEach>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                <button type="button" class="btn btn-primary" id="sendInviteBtn">초대 보내기</button>
            </div>
        </div>
    </div>
</div>


