document.addEventListener("DOMContentLoaded", () => {
    const inviteBtn = document.getElementById("openInviteModal");
    if (!inviteBtn) {
        console.warn("openInviteModal 버튼을 찾을 수 없습니다.");
        return;
    }

    const ctx = window.APP_CTX || ""; // 컨텍스트 경로 사용

    inviteBtn.addEventListener("click", (e) => {
        e.preventDefault();
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

                const modal = new bootstrap.Modal(modalElement);
                modal.show();

                modalElement.addEventListener("hidden.bs.modal", () => wrapper.remove());
            })
            .catch(err => {
                console.error("/invite/modal 요청 실패:", err);
            });
    });
});
