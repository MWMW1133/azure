// ===== 유틸 =====
const $ = (sel, root = document) => root.querySelector(sel);
const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));

function getCtxAndProject() {
  const root = document.getElementById('project-tab-root') || document.getElementById('project-tab-view-root') || document.querySelector('.management-wrapper'); // fragment 폴백
  const ctx = (root?.dataset.ctx || root?.dataset.contextPath || '').replace(/\/$/, '');
  const projectId = root?.dataset.projectId;
  return { ctx, projectId, root };
}

function getCsrf() {
  const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
  const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';
  return token ? { header, token } : null;
}

async function http(method, url, body) {
  const csrf = getCsrf();
  const headers = { Accept: 'application/json' };
  if (body != null) headers['Content-Type'] = 'application/json';
  if (csrf) headers[csrf.header] = csrf.token;

  const resp = await fetch(url, {
    method,
    headers,
    body: body != null ? JSON.stringify(body) : undefined,
    credentials: 'same-origin',
    cache: 'no-cache',
  });

  if (!resp.ok) {
    const text = await resp.text().catch(() => '');
    const err = new Error(`HTTP ${resp.status}: ${text || resp.statusText}`);
    err.status = resp.status;
    throw err;
  }
  const ct = resp.headers.get('content-type') || '';
  if (ct.includes('application/json')) return resp.json();
  return null;
}

// ===== 렌더 =====
function renderSkeleton(container, count = 6) {
  container.innerHTML = Array.from({ length: count })
    .map(
      () => `
      <div class="member-row" aria-hidden="true" style="opacity:.6">
        <div class="member-initial" style="background:#eef2f7;">&nbsp;</div>
        <div class="member-name" style="height:16px; width:40%; background:#f1f5f9; border-radius:6px;"></div>
        <button class="management-member-action" disabled>삭제</button>
      </div>
    `
    )
    .join('');
}

function renderEmpty(container) {
  container.innerHTML = `<div class="empty-hint">멤버가 존재하지 않습니다.</div>`;
}

function renderMembers(container, pageData) {
  const content = pageData?.content || [];
  if (content.length === 0) return renderEmpty(container);

  container.innerHTML = content
    .map((m) => {
      const id = m.userId;
      const name = m.userName;
      const avatar = m.userAvatarUrl;
      const initial = name && name.length > 0 ? name.substring(0, 1) : '?';
      return `
        <div class="member-row" data-member-id="${id}">
          ${avatar ? `<img src="${avatar}" alt="${name}" class="member-img">` : `<div class="member-initial" title="${name}">${initial}</div>`}
          <div class="member-name">${escapeHtml(name)}</div>
          <button class="management-member-action" data-member-id="${id}" aria-label="${name} 삭제">삭제</button>
        </div>
      `;
    })
    .join('');
}

// 간단한 XSS 방지용
function escapeHtml(s) {
  return String(s ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');
}

// 멤버 로드
async function loadMembers() {
  const { ctx, projectId } = getCtxAndProject();
  const body = $('.management-member-body');
  if (!body) return;
  renderSkeleton(body);

  const url = `${ctx}/projects/${projectId}/members/list?size=200&sort=userName,asc`;
  try {
    const page = await http('GET', url);
    renderMembers(body, page);
  } catch (e) {
    body.innerHTML = `<div class="empty-hint">멤버를 불러오지 못했습니다.<br><small>${escapeHtml(e.message)}</small></div>`;
  }
}

async function removeMember(userId) {
  const { ctx, projectId } = getCtxAndProject();
  const url = `${ctx}/projects/${projectId}/members/${userId}`;
  await http('DELETE', url);
}

async function deleteProject() {
  const { ctx, projectId } = getCtxAndProject();
  const url = `${ctx}/projects/${projectId}`;
  await http('DELETE', url);
}

// ===== 이벤트 바인딩 =====
function bindEvents() {
  const root = document;
  // 멤버 삭제
  root.addEventListener('click', async (e) => {
    const btn = e.target.closest('.management-member-action');
    if (!btn) return;

    const row = btn.closest('.member-row');
    const userId = btn.dataset.memberId || row?.dataset.memberId;
    const name = row?.querySelector('.member-name')?.textContent?.trim() || '';

    const ok = confirm(`멤버 "${name}"(ID: ${userId})를 삭제하시겠습니까?\n해당 사용자는 이 프로젝트에 접근할 수 없게 됩니다.`);
    if (!ok) return;

    btn.disabled = true;
    btn.textContent = '삭제 중...';
    try {
      await removeMember(userId);
      row?.remove();
      const body = $('.management-member-body');
      if (body && body.children.length === 0) renderEmpty(body);
    } catch (e2) {
      alert(`삭제 실패: ${e2.message}`);
      btn.disabled = false;
      btn.textContent = '삭제';
    }
  });

  // 프로젝트 삭제
  root.addEventListener('click', async (e) => {
    const btn = e.target.closest('.management-project-delete');
    if (!btn) return;

    const really = confirm('정말 프로젝트를 삭제하시겠습니까?\n' + '모든 태스크/파일/권한이 함께 제거될 수 있습니다.\n\n');
    if (!really) return;

    btn.disabled = true;
    btn.textContent = '삭제 중...';
    try {
      await deleteProject();
      alert('프로젝트가 삭제되었습니다.');
      // 목록 페이지로 이동하거나 홈으로
      window.location.href = `${getCtxAndProject().ctx}/home`;
    } catch (e2) {
      alert(`프로젝트 삭제 실패: ${e2.message}`);
      btn.disabled = false;
      btn.textContent = '프로젝트 삭제';
    }
  });
}

// ===== 초기화 =====
function initManagementTab() {
  const wrap = document.querySelector('.management-wrapper');
  if (!wrap) return;
  if (wrap.dataset.mgmtInit === '1') return;

  bindEvents();
  loadMembers();
  wrap.dataset.mgmtInit = '1';
}

// DOM 준비 시 실행
document.addEventListener('DOMContentLoaded', initManagementTab);
