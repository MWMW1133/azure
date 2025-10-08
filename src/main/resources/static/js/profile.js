document.addEventListener("DOMContentLoaded", () => {
    // [1] Settings → submenu 열기 (1단→2단)
    document.querySelectorAll('.dropdown-submenu > a').forEach((menu) => {
        menu.addEventListener("click", (e) => {
            e.preventDefault();
            e.stopPropagation();

            const submenu = menu.nextElementSibling;

            // 다른 submenu 닫기
            document.querySelectorAll('.dropdown-submenu .dropdown-menu').forEach((sm) => {
                if (sm !== submenu) sm.classList.remove("show");
            });

            submenu.classList.toggle("show");
        });
    });

    // [2] collapse 버튼 누르면 dropdown 닫히지 않도록
    document.querySelectorAll('[data-bs-toggle="collapse"]').forEach((toggle) => {
        toggle.addEventListener("click", (e) => {
            e.stopPropagation();
        });
    });

    // [3] Change Password → 모달 열기
    const changePwdItem = document.querySelector('.dropdown-item.change-password');
    if (changePwdItem) {
        changePwdItem.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            const modalEl = document.getElementById('changePwdModal');
            if (modalEl) new bootstrap.Modal(modalEl).show();
        });
    }

    // [4] 드롭다운 닫히면 전부 초기화
    document.querySelectorAll('.dropdown').forEach((dropdown) => {
        dropdown.addEventListener("hide.bs.dropdown", () => {
            dropdown.querySelectorAll('.dropdown-submenu .dropdown-menu').forEach((sm) => {
                sm.classList.remove("show");
            });
        });
    });
});
