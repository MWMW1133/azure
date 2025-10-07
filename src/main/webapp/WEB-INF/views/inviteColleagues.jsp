<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="modal fade" id="inviteModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">회사 초대</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <div class="modal-body">
                <input type="text" id="inviteSearch" class="form-control mb-3"
                       placeholder="이름 또는 ID를 검색하세요.">

                <div id="inviteList" class="list-group">
                    <c:forEach var="u" items="${invitableUsers}">
                        <div class="list-group-item d-flex align-items-center justify-content-between">
                            <div>
                                <img src="${pageContext.request.contextPath}${u.avatarUrl}" class="rounded-circle me-2" width="32" height="32">
                                <span>${u.name}</span>
                                <small class="text-muted ms-2">(${u.loginId})</small>
                            </div>
                            <input type="checkbox" class="form-check-input" value="${u.id}">
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

<script>
    (function () {
        var ctx = window.APP_CTX || '';
        var input = document.getElementById("inviteSearch");
        if (!input) return;

        input.addEventListener("input", function () {
            var keyword = this.value.trim();
            var url = ctx + '/invite/search?keyword=' + encodeURIComponent(keyword);

            fetch(url)
                .then(function(res){ return res.json(); })
                .then(function(data){
                    var list = document.getElementById("inviteList");
                    list.innerHTML = "";
                    data.forEach(function(u){
                        var imgSrc = (u.avatarUrl ? (ctx + u.avatarUrl) : (ctx + '/images/default-avatar.png'));
                        list.innerHTML +=
                            '<div class="list-group-item d-flex align-items-center justify-content-between">'
                            + '<div>'
                            +   '<img src="' + imgSrc + '" class="rounded-circle me-2" width="32" height="32">'
                            +   '<span>' + (u.name || '') + '</span>'
                            +   '<small class="text-muted ms-2">(' + (u.loginId || '') + ')</small>'
                            + '</div>'
                            + '<input type="checkbox" class="form-check-input" value="' + u.id + '">'
                            + '</div>';
                    });
                });
        });
    })();
</script>

