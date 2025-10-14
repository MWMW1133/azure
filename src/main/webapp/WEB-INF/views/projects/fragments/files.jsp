<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/files.css">

<!-- 삭제하면 주긴다 -->
<script>
    window.USER_ID = '${user.id}';
    window.APP_CONTEXT = '${pageContext.request.contextPath}';
</script>

<script defer src="${pageContext.request.contextPath}/js/files.js"></script>

<div class="project-files p-3">

    <!-- 헤더 -->
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h6 class="fw-bold mb-0">파일 목록</h6>
        <div class="d-flex align-items-center gap-2">
            <input type="text" id="fileSearchInput" class="form-control form-control-sm"
                   placeholder="검색" style="width: 200px;">
            <button type="button" class="btn btn-sm btn-primary" id="uploadBtn">
                <i class="bi bi-upload"></i> 파일 업로드
            </button>
        </div>
    </div>

    <!-- 파일 카드 영역 -->
    <div id="fileGrid" class="file-grid d-flex flex-wrap gap-3">
        <c:forEach var="d" items="${documents}">
            <div class="file-card border rounded p-3 text-center shadow-sm"
                 style="width: 160px; cursor: pointer;">

                <!-- ⋮ 점 세 개 메뉴 -->
                <div class="file-menu position-absolute top-0 end-0 me-1 mt-1">
                    <button class="menu-btn btn btn-sm btn-light" data-file-id="${d.id}">
                        <i class="bi bi-three-dots-vertical"></i>
                    </button>
                    <ul class="menu-dropdown shadow-sm">
                        <li data-action="view">파일 보기</li>
                        <li data-action="download">파일 다운로드</li>
                        <li data-action="delete" class="text-danger">파일 삭제</li>
                    </ul>
                </div>

                <!-- 파일 아이콘 -->
                <div class="file-icon mb-2">
                    <i class="bi bi-file-earmark-text fs-1 text-secondary"></i>
                </div>

                <!-- 파일 이름 -->
                <div class="file-name small text-truncate">
                    <c:choose>
                        <c:when test="${not empty d.tempTask}">
                            ${d.tempTask.title} <small class="text-muted">(${d.title})</small>
                        </c:when>
                        <c:otherwise>
                            ${d.title}
                        </c:otherwise>
                    </c:choose>
                </div>


                <!-- 작성자 -->
                <div class="file-author small text-muted">
                        ${d.author.name}
                </div>

                <!-- 생성일 -->
                <div class="file-date small text-muted">
                        ${d.createdAt.toLocalDate()}
                </div>
            </div>
        </c:forEach>

        <c:if test="${empty documents}">
            <p class="text-muted small">등록된 문서가 없습니다.</p>
        </c:if>

        <!--  템플릿 선택 카드 -->
        <div class="file-upload-card border border-dashed rounded p-3 text-center text-muted"
             style="width: 160px; cursor: pointer;"
             data-bs-toggle="modal" data-bs-target="#templateModal">
            <i class="bi bi-plus-lg fs-2"></i>
            <div class="small">템플릿 선택</div>
        </div>
    </div>

</div>

<!-- 템플릿 선택 모달 -->
<div class="modal fade" id="templateModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-semibold">템플릿 선택</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body d-flex flex-wrap gap-3">
                <div class="border rounded p-3 text-center flex-fill template-card" style="cursor:pointer;">
                    <i class="bi bi-journal-text fs-2 mb-2"></i><br>회의록 양식
                </div>
                <div class="border rounded p-3 text-center flex-fill template-card" style="cursor:pointer;">
                    <i class="bi bi-file-earmark-bar-graph fs-2 mb-2"></i><br>프로젝트 보고서
                </div>
                <div class="border rounded p-3 text-center flex-fill template-card" style="cursor:pointer;">
                    <i class="bi bi-pencil-square fs-2 mb-2"></i><br>기획 문서
                </div>
            </div>
        </div>
    </div>
</div>
