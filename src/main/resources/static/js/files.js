// window.addEventListener('load', bindUploadHandler);
//
// function bindUploadHandler() {
//
//     const userId = Number(window.USER_ID);
//     const uploadBtn = document.getElementById('uploadBtn');
//     const searchInput = document.getElementById('fileSearchInput');
//     const ctx = window.APP_CONTEXT || '';
//     const projectId =
//         window.PROJECT_ID ||
//         document.getElementById('project-tab-root')?.dataset.projectId;
//
//     // 아직 버튼이 안 그려졌다면 0.3초 후 재시도
//     if (!uploadBtn) {
//         setTimeout(bindUploadHandler, 300);
//         return;
//     }
//
//     console.log('[files.js] uploadBtn found ✅');
//
//     // 🔸 파일 업로드 버튼 클릭
//     uploadBtn.addEventListener('click', (e) => {
//         e.preventDefault();
//         e.stopPropagation();
//
//         const input = document.createElement('input');
//         input.type = 'file';
//         input.accept = '*/*';
//         input.onchange = async (e) => {
//             const file = e.target.files[0];
//             if (!file) return;
//
//             const formData = new FormData();
//             formData.append('file', file);
//             formData.append('authorId', userId);
//
//             console.log(`[files.js] 업로드 시작: ${file.name}, authorId=${userId}`);
//
//             const res = await fetch(`${ctx}/projects/${projectId}/documents/upload`, {
//                 method: 'POST',
//                 body: formData
//             });
//
//             console.log(`[DEBUG] upload URL = ${ctx}/projects/${projectId}/documents/upload`);
//
//             if (res.ok) {
//                 console.log('[files.js] 업로드 성공, fragment 새로고침 중...');
//                 const html = await res.text();
//
//                 const parser = new DOMParser();
//                 const doc = parser.parseFromString(html, 'text/html');
//                 const body = doc.body;
//                 const newCards = body.querySelectorAll('#fileGrid .file-card');
//                 const currentGrid = document.querySelector('#fileGrid');
//
//                 if (newCards.length > 0 && currentGrid) {
//                     newCards.forEach(card => {
//                         currentGrid.prepend(card.cloneNode(true));
//                     });
//                     console.log('[files.js] 업로드 성공 새 파일 추가');
//                 } else {
//                     console.warn('[files.js] fileGrid or newCards not found');
//                 }
//             } else {
//                 alert('업로드 실패');
//             }
//         };
//
//         // 탐색기 창 열기
//         input.click();
//     });
//
//     // 🔸 검색 필터
//     if (searchInput) {
//         searchInput.addEventListener('input', () => {
//             const q = searchInput.value.toLowerCase();
//             document.querySelectorAll('.file-card').forEach(card => {
//                 const name = card.querySelector('.file-name').textContent.toLowerCase();
//                 card.style.display = name.includes(q) ? '' : 'none';
//             });
//         });
//     }
//
//     // 점 세 개 메뉴 열기/닫기 + 액션 처리
//     document.addEventListener('click', (e) => {
//         // 메뉴 버튼 클릭 시 열기
//         const btn = e.target.closest('.menu-btn');
//         const allMenus = document.querySelectorAll('.file-menu');
//         allMenus.forEach(m => m.classList.remove('open'));
//
//         if (btn) {
//             btn.parentElement.classList.toggle('open');
//             e.stopPropagation();
//             return;
//         }
//
//         // 메뉴 아이템 클릭 시
//         const item = e.target.closest('.menu-dropdown li');
//         if (!item) return;
//
//         const action = item.dataset.action;
//         const fileId = item.closest('.file-menu').querySelector('.menu-btn').dataset.fileId;
//         const ctx = window.APP_CONTEXT || '';
//
//         if (action === 'view') {
//             window.open(`${ctx}/files/${fileId}/view`, '_blank');
//         } else if (action === 'download') {
//             window.location.href = `${ctx}/files/${fileId}/download`;
//         }
//     });
//
// }
(() => {
    // 유틸
    const $ = (sel, root = document) => root.querySelector(sel);
    const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));

    const ctx = window.APP_CONTEXT || '';
    // console.log('[DEBUG] window.USER_ID at top of files.js =', window.USER_ID, typeof window.USER_ID);
    // // const userId = parseInt(window.USER_ID, 10);
    // const currentUserId = parseInt(window.USER_ID, 10);
    // if (isNaN(currentUserId) || currentUserId <= 0) {
    //     console.error('[files.js] USER_ID invalid during upload:', window.USER_ID);
    //     alert('로그인 정보가 없습니다. 다시 로그인해주세요.');
    //     return;
    // }
    // console.log('[DEBUG] parsed userId =', userId);

    const projectId =
        window.PROJECT_ID ||
        document.getElementById('project-tab-root')?.dataset.projectId;

    /**
     * 📌 파일 탭 로드될 때 실행되는 메인 함수
     */
    function initFileTab() {
        const uploadBtn = $('#uploadBtn');
        const searchInput = $('#fileSearchInput');
        const fileGrid = $('#fileGrid');

        if (!uploadBtn || !fileGrid) {
            setTimeout(initFileTab, 300);
            return;
        }

        console.log('[files.js] uploadBtn found ✅');

        // --- 업로드 버튼 이벤트 등록 ---
        uploadBtn.onclick = async (e) => {
            e.preventDefault();
            e.stopPropagation();

            console.log('[DEBUG] window.USER_ID at top of files.js =', window.USER_ID, typeof window.USER_ID);
            // const userId = parseInt(window.USER_ID, 10);
            const currentUserId = parseInt(window.USER_ID, 10);
            if (isNaN(currentUserId) || currentUserId <= 0) {
                console.error('[files.js] USER_ID invalid during upload:', window.USER_ID);
                alert('로그인 정보가 없습니다. 다시 로그인해주세요.');
                return;
            }
            console.log('[DEBUG] parsed userId =', currentUserId);

            const input = document.createElement('input');
            input.type = 'file';
            input.accept = '*/*';
            input.onchange = async (e) => {
                const file = e.target.files[0];
                if (!file) return;

                const formData = new FormData();
                formData.append('file', file);
                formData.append('authorId', currentUserId);

                console.log(`[files.js] 업로드 시작: ${file.name}, authorId=${currentUserId}`);

                try {
                    const res = await fetch(`${ctx}/projects/${projectId}/documents/upload`, {
                        method: 'POST',
                        body: formData
                    });

                    console.log(`[DEBUG] upload URL = ${ctx}/projects/${projectId}/documents/upload`);

                    if (!res.ok) throw new Error('업로드 실패');

                    const html = await res.text();
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(html, 'text/html');
                    const newCards = doc.querySelectorAll('#fileGrid .file-card');
                    const currentGrid = document.querySelector('#fileGrid');

                    if (newCards.length && currentGrid) {
                        newCards.forEach(card => {
                            currentGrid.prepend(card.cloneNode(true));
                        });
                        console.log('[files.js] 업로드 성공 새 파일 추가');
                    } else {
                        console.warn('[files.js] fileGrid or newCards not found');
                    }
                } catch (err) {
                    console.error('[files.js] 업로드 실패:', err);
                    alert('파일 업로드 중 오류가 발생했습니다.');
                }
            };

            input.click();
        };

        // --- 검색 필터 ---
        if (searchInput) {
            searchInput.addEventListener('input', () => {
                const q = searchInput.value.toLowerCase();
                $$('.file-card').forEach((card) => {
                    const name = card.querySelector('.file-name').textContent.toLowerCase();
                    card.style.display = name.includes(q) ? '' : 'none';
                });
            });
        }

        // --- 점 세개 메뉴 ---
        document.addEventListener('click', (e) => {
            const btn = e.target.closest('.menu-btn');
            const allMenus = document.querySelectorAll('.file-menu');
            allMenus.forEach((m) => m.classList.remove('open'));

            if (btn) {
                btn.parentElement.classList.toggle('open');
                e.stopPropagation();
                return;
            }

            const item = e.target.closest('.menu-dropdown li');
            if (!item) return;

            const action = item.dataset.action;
            const fileId = item.closest('.file-menu').querySelector('.menu-btn').dataset.fileId;

            if (action === 'view') {
                window.open(`${ctx}/files/${fileId}/view`, '_blank');
            } else if (action === 'download') {
                window.location.href = `${ctx}/files/${fileId}/download`;
            }
        });
    }

    function watchFileTabLoad() {
        const observer = new MutationObserver(() => {
            const tab = document.querySelector('#fileGrid');
            if (tab && !tab.dataset.bound) {
                tab.dataset.bound = 'true';
                initFileTab();
            }
        });

        observer.observe(document.body, { childList: true, subtree: true });
        console.log('[files.js] MutationObserver watching for file tab load');
    }

    // 최초 진입 시에도 실행
    document.addEventListener('DOMContentLoaded', () => {
        initFileTab();
        watchFileTabLoad();
    });
})();
