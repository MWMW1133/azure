document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('changePwdForm');
  const btnSave = document.getElementById('btnChangePwdSave');
  const alertBox = document.getElementById('changePwdAlert');
  const currentEl = document.getElementById('currentPassword');
  const newEl = document.getElementById('newPassword');
  const confirmEl = document.getElementById('confirmPassword');
  const matchHelp = document.getElementById('pwdMatchHelp');
  const btnTopSave = document.querySelector('.position-absolute .btn-primary.btn-sm');
  const nameEl = document.getElementById('fullName');
  const statusEl = document.getElementById('workStatus');
  const avatarEl = document.getElementById('profileImage');
  const root = document.getElementById('profile-root');
  const CTX = root?.dataset.ctx || '';
  const csrfToken = root?.dataset.csrfToken;
  const csrfHeader = root?.dataset.csrfHeader || 'X-CSRF-TOKEN';
  // [1] Settings → submenu 열기 (1단→2단)
  document.querySelectorAll('.dropdown-submenu > a').forEach((menu) => {
    menu.addEventListener('click', (e) => {
      e.preventDefault();
      e.stopPropagation();

      const submenu = menu.nextElementSibling;

      // 다른 submenu 닫기
      document.querySelectorAll('.dropdown-submenu .dropdown-menu').forEach((sm) => {
        if (sm !== submenu) sm.classList.remove('show');
      });

      submenu.classList.toggle('show');
    });
  });

  // [2] collapse 버튼 누르면 dropdown 닫히지 않도록
  document.querySelectorAll('[data-bs-toggle="collapse"]').forEach((toggle) => {
    toggle.addEventListener('click', (e) => {
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
    dropdown.addEventListener('hide.bs.dropdown', () => {
      dropdown.querySelectorAll('.dropdown-submenu .dropdown-menu').forEach((sm) => {
        sm.classList.remove('show');
      });
    });
  });

  //비밀번호 변경
  function showAlert(type, msg) {
    alertBox.className = `alert alert-${type}`;
    alertBox.textContent = msg;
    alertBox.classList.remove('d-none');
  }
  function clearAlert() {
    alertBox.className = 'alert d-none';
    alertBox.textContent = '';
  }

  // 입력 즉시 일치 여부 안내
  function syncMatchHint() {
    if (!newEl.value || !confirmEl.value) {
      matchHelp.textContent = '';
      matchHelp.className = 'form-text';
      return;
    }
    if (newEl.value === confirmEl.value) {
      matchHelp.textContent = '비밀번호가 일치합니다.';
      matchHelp.className = 'form-text text-success';
    } else {
      matchHelp.textContent = '비밀번호가 일치하지 않습니다.';
      matchHelp.className = 'form-text text-danger';
    }
  }
  newEl.addEventListener('input', syncMatchHint);
  confirmEl.addEventListener('input', syncMatchHint);

  async function changePassword() {
    clearAlert();

    // 기본 검사
    if (!form.reportValidity()) return;
    if (newEl.value !== confirmEl.value) {
      showAlert('danger', '새 비밀번호와 확인이 일치하지 않습니다.');
      confirmEl.focus();
      return;
    }
    if (newEl.value.length < 8) {
      showAlert('warning', '새 비밀번호는 8자 이상으로 설정해주세요.');
      newEl.focus();
      return;
    }
    // 비번 추가 조건 걸 예정

    btnSave.disabled = true;
    btnSave.dataset.originalText = btnSave.textContent;
    btnSave.textContent = '저장 중...';

    try {
      const res = await fetch(`${CTX}/api/users/password`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          ...(csrfToken ? { [csrfHeader]: csrfToken } : {}),
          'X-Requested-With': 'XMLHttpRequest',
        },
        credentials: 'same-origin',
        body: JSON.stringify({
          currentPassword: currentEl.value,
          newPassword: newEl.value,
        }),
        cache: 'no-cache',
      });

      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        const msg = data?.message || data?.error || `비밀번호 변경 실패 (HTTP ${res.status})`;
        showAlert('danger', msg);
        return;
      }

      showAlert('success', '비밀번호가 변경되었습니다. 잠시 후 로그아웃됩니다.');
      form.reset();
      syncMatchHint();

      // 1~1.5초 뒤 자동 로그아웃
      setTimeout(() => {
        logoutAndRedirect();
      }, 1200);

      // 잠시 후 모달 닫기
      setTimeout(() => {
        const modalEl = document.getElementById('changePwdModal');
        if (modalEl) bootstrap.Modal.getOrCreateInstance(modalEl).hide();
        clearAlert();
      }, 900);
    } catch (e) {
      showAlert('danger', '네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
    } finally {
      btnSave.disabled = false;
      btnSave.textContent = btnSave.dataset.originalText || '저장';
    }
  }

  btnSave?.addEventListener('click', changePassword);

  async function logoutAndRedirect() {
    try {
      await fetch(`${CTX}/logout`, {
        method: 'POST',
        headers: {
          ...(csrfToken ? { [csrfHeader]: csrfToken } : {}),
          'X-Requested-With': 'XMLHttpRequest',
        },
        credentials: 'same-origin',
      });
    } catch (e) {
    } finally {
      window.location.href = `${CTX}/login?passwordChanged=1`;
    }
  }

  async function uploadAvatarIfNeeded() {
    const file = avatarEl?.files?.[0];
    if (!file) return null;

    const fd = new FormData();
    fd.append('file', file);

    const res = await fetch(`${CTX}/api/users/avatar`, {
      method: 'POST',
      headers: {
        ...(csrfToken ? { [csrfHeader]: csrfToken } : {}),
        'X-Requested-With': 'XMLHttpRequest',
      },
      credentials: 'same-origin',
      body: fd,
    });
    if (!res.ok) {
      const text = await res.text().catch(() => '');
      throw new Error(`아바타 업로드 실패(HTTP ${res.status}) ${text}`);
    }
    const data = await res.json();
    return data?.url || null; // 서버가 {url:"/uploads/xxx.png"} 반환
  }

  async function saveProfile() {
    btnTopSave.disabled = true;
    const body = {
      name: nameEl?.value?.trim() || null,
      workStatus: statusEl?.value || null, // 서버에서 enum 매칭 실패하면 무시하도록 처리
      avatarUrl: null,
    };

    try {
      const avatarUrl = await uploadAvatarIfNeeded();
      if (avatarUrl) body.avatarUrl = avatarUrl;

      const res = await fetch(`${CTX}/api/users/profile`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          ...(csrfToken ? { [csrfHeader]: csrfToken } : {}),
          'X-Requested-With': 'XMLHttpRequest',
        },
        credentials: 'same-origin',
        body: JSON.stringify(body),
      });

      if (!res.ok) {
        let msg = `프로필 저장 실패 (HTTP ${res.status})`;
        try {
          const d = await res.json();
          if (d?.message) msg = d.message;
        } catch {}
        alert(msg);
        return;
      }

      alert('프로필이 저장되었습니다.');
      // 필요하면 새로고침 or 화면 값 갱신
      if (body.avatarUrl) {
        // 즉시 이미지 미리보기 교체
        const img = document.querySelector('img[alt="Profile"]');
        if (img) img.src = `${CTX}${body.avatarUrl}`;
        avatarEl.value = ''; // 파일 입력 초기화
      }
      if (body.name) {
        const h5 = document.querySelector('.d-flex.align-items-center h5');
        if (h5) h5.textContent = body.name;
      }
    } catch (e) {
      console.error(e);
      alert('네트워크 오류가 발생했습니다.');
    } finally {
      btnTopSave.disabled = false;
    }
  }

  btnTopSave?.addEventListener('click', saveProfile);
});
