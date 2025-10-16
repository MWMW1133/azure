// 디버깅 로그 지우지마세욧~~! 그리고 파일 관련 로직 프론트, 백엔드
// 둘 다 함부로 건들면 안돌아갈거에요
(() => {
    const $ = (sel, root = document) => root.querySelector(sel);
    const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));

    const ctx = window.APP_CONTEXT || '';
    const projectId =
        window.PROJECT_ID ||
        document.getElementById('project-tab-root')?.dataset.projectId;

    let fileMenuListenerBound = false; //  전역에 위치

    // ==========================================
    //  파일 메뉴 클릭 핸들러 (delete, view, download)
    // ==========================================
    async function handleFileMenuClick(e) {
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
            window.open(`${ctx}/api/files/${fileId}/view`, '_blank');
        } else if (action === 'download') {
            window.location.href = `${ctx}/api/files/${fileId}/download`;
        } else if (action === 'delete') {
            if (confirm('이 파일을 삭제하시겠습니까?')) {
                try {
                    const res = await fetch(`${ctx}/api/files/${fileId}`, { method: 'DELETE' });

                    if (res.ok) {
                        console.log(`[files.js] 파일 ${fileId} 삭제 성공 — 목록 리로드 중`);

                        const refreshed = await fetch(`${ctx}/projects/${projectId}/documents`);
                        const html = await refreshed.text();
                        const parser = new DOMParser();
                        const doc = parser.parseFromString(html, 'text/html');
                        const newGrid = doc.querySelector('#fileGrid');
                        const currentGrid = document.querySelector('#fileGrid');

                        if (newGrid && currentGrid) {
                            currentGrid.innerHTML = newGrid.innerHTML;
                            console.log('[files.js] 파일 목록 갱신 완료 ✅');

                            setTimeout(() => {
                                initFileTab();
                                console.log('파일 탭 리렌더링');
                            }, 200);
                        }
                    } else {
                        alert('삭제 중 오류가 발생했습니다.');
                    }
                } catch (err) {
                    console.error('[files.js] 삭제 실패:', err);
                    alert('서버 오류로 삭제할 수 없습니다.');
                }
            }
        }
    }

    // ==========================================
    // 파일 탭 초기화
    // ==========================================
    function initFileTab() {
        const uploadBtn = $('#uploadBtn');
        const searchInput = $('#fileSearchInput');
        const fileGrid = $('#fileGrid');

        if (!uploadBtn || !fileGrid) {
            setTimeout(initFileTab, 300);
            return;
        }

        console.log('[files.js] uploadBtn found ✅');

        // --- 업로드 버튼 ---
        uploadBtn.onclick = async (e) => {
            e.preventDefault();
            e.stopPropagation();
            const currentUserId = parseInt(window.USER_ID, 10);
            if (isNaN(currentUserId) || currentUserId <= 0) {
                alert('로그인 정보가 없습니다. 다시 로그인해주세요.');
                return;
            }

            const input = document.createElement('input');
            input.type = 'file';
            input.accept = '*/*';
            input.onchange = async (e) => {
                const file = e.target.files[0];
                if (!file) return;
                const formData = new FormData();
                formData.append('file', file);
                formData.append('authorId', currentUserId);

                try {
                    const res = await fetch(`${ctx}/projects/${projectId}/documents/upload`, {
                        method: 'POST',
                        body: formData
                    });
                    if (!res.ok) throw new Error('업로드 실패');
                    const html = await res.text();
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(html, 'text/html');
                    const newGrid = doc.querySelector('#fileGrid');
                    const currentGrid = document.querySelector('#fileGrid');
                    if (newGrid && currentGrid) {
                        currentGrid.innerHTML = newGrid.innerHTML;
                        console.log('[files.js] 업로드 성공 후 목록 갱신 완료 ✅');
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

        // --- 점 세개 메뉴 리스너 등록 (한 번만) ---
        if (!fileMenuListenerBound) {
            document.addEventListener('click', handleFileMenuClick);
            fileMenuListenerBound = true;
            console.log('[files.js] 파일 메뉴 리스너 등록 완료 ✅');
        }
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


    //  수정
    // window.selectTemplate = function(templateKey) {
    //     const projectId = window.PROJECT_ID || null;
    //     if (!projectId) {
    //         console.error("PROJECT_ID not found");
    //         return;
    //     }
    //     window.location.href = `/projects/${projectId}/documents/new?templateKey=${templateKey}`;
    // };
    window.selectTemplate = function (templateKey) {
        // 우선 window.PROJECT_ID 시도
        let projectId = window.PROJECT_ID;

        // 없다면 DOM에서 추출
        if (!projectId) {
            const grid = document.querySelector('#fileGrid');
            if (grid) {
                // URL 경로나 data-* 속성에서 추출
                const match = window.location.pathname.match(/projects\/(\d+)/);
                if (match) {
                    projectId = match[1];
                }
            }
        }

        if (!projectId) {
            console.error("PROJECT_ID not found even after fallback");
            return;
        }

        window.location.href = `/projects/${projectId}/documents/new?templateKey=${templateKey}`;
    };



    document.addEventListener('DOMContentLoaded', () => {
        initFileTab();
        watchFileTabLoad();
    });
})();
