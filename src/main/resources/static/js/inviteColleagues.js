document.addEventListener("DOMContentLoaded", () => {
    const ctx = window.APP_CTX ?? "";

    /** ===============================
     *  [1] Invite 버튼 클릭 → 모달 fetch
     * =============================== */
    const inviteBtn = document.getElementById("openInviteModal");
    if (!inviteBtn) {
        console.warn("openInviteModal 버튼을 찾을 수 없습니다.");
        return;
    }

    inviteBtn.addEventListener("click", (e) => {
        e.preventDefault();

        // 관리자만 할 수 있도록
        if (userRole !== "MANAGER" && userRole !== "LEADER") {
            alert("초대 권한이 없습니다. 관리자만 초대할 수 있습니다.");
            return;
        }

        console.log("[DEBUG] Invite colleagues 버튼 클릭됨");

        fetch(`${ctx}/invite/modal`)
            .then(res => {
                if (!res.ok) throw new Error("서버 응답 오류: " + res.status);
                return res.text();
            })
            .then(html => {
                const wrapper = document.createElement("div");
                wrapper.innerHTML = html;
                document.body.appendChild(wrapper);

                const modalElement = document.getElementById("inviteModal");
                if (!modalElement) {
                    console.error("inviteModal 요소를 찾지 못했습니다.");
                    return;
                }

                // Modal 실행
                const modal = new bootstrap.Modal(modalElement);
                modal.show();

                // 모달 닫힐 때 DOM에서 제거
                modalElement.addEventListener("hidden.bs.modal", () => wrapper.remove());

                // 모달 내부 스크립트 초기화
                initInviteModal(ctx);
            })
            .catch(err => {
                console.error("/invite/modal 요청 실패:", err);
            });
    });


    /** ==========================================
     *  [2] 모달 내부 동작 (검색 & 엔터키 이벤트)
     * ========================================== */
    function initInviteModal(ctx) {
        const input = document.getElementById("inviteSearch");
        const list = document.getElementById("inviteList");

        if (!input || !list) {
            console.warn("inviteModal 내부 요소를 찾을 수 없습니다.");
            return;
        }

        // [1] 실시간 검색
        input.addEventListener("input", function () {
            const keyword = this.value.trim();
            if (!keyword) {
                list.innerHTML = "";
                return;
            }
            performSearch(keyword);
        });

        // [2] 엔터키로 검색 실행
        input.addEventListener("keydown", function (e) {
            if (e.key === "Enter") {
                e.preventDefault();
                const keyword = this.value.trim();
                if (keyword) performSearch(keyword);
            }
        });

        // [3] 검색 로직
        function performSearch(keyword) {
            fetch(`${ctx}/invite/search?keyword=${encodeURIComponent(keyword)}`)
                .then(res => res.json())
                .then(data => {
                    list.innerHTML = "";
                    if (data.length === 0) {
                        list.innerHTML = `<div class="text-muted p-3 text-center">검색 결과가 없습니다.</div>`;
                        return;
                    }
                    data.forEach(u => {
                        const avatar = u.avatarUrl ? (ctx + u.avatarUrl) : (ctx + "/images/default-avatar.png");
                        list.innerHTML += `
                            <div class="list-group-item d-flex align-items-center justify-content-between">
                                <div>
                                    <img src="${avatar}" class="rounded-circle me-2" width="32" height="32">
                                    <span>${u.name}</span>
                                    <small class="text-muted ms-2">(${u.loginId})</small>
                                </div>
                                <input type="checkbox" class="form-check-input" value="${u.id}">
                            </div>`;
                    });
                })
                .catch(err => {
                    console.error("검색 실패:", err);
                    list.innerHTML = `<div class="text-danger p-3 text-center">검색 중 오류가 발생했습니다.</div>`;
                });
        }
    }

    document.addEventListener("click", (e) => {
        if (e.target.id === "sendInviteBtn") {
            e.preventDefault();
            const checked = document.querySelectorAll("#inviteList input[type='checkbox']:checked");
            if (checked.length === 0) {
                alert("초대할 사용자를 선택하세요.");
                return;
            }

            const ids = Array.from(checked).map(chk => Number(chk.value)); // 숫자 변환
            console.log("[DEBUG] 초대 대상:", ids);

            fetch(`${ctx}/invite/send`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json; charset=utf-8"
                },
                body: JSON.stringify({ userIds: ids })
            })
                .then(res => res.json())
                .then(data => {
                    if (data.status === "success") {
                        alert("초대가 전송되었습니다!");
                        bootstrap.Modal.getInstance(document.getElementById("inviteModal")).hide();
                    } else {
                        alert("초대 전송 실패: " + data.message);
                    }
                })
                .catch(err => {
                    console.error("초대 전송 중 오류:", err);
                    alert("서버 오류가 발생했습니다.");
                });
        }
    });

});

