// resources/static/admin/js/admin-login.js

document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const loginBtn = document.getElementById('loginBtn');
    const adminIdInput = document.getElementById('adminId');
    const passwordInput = document.getElementById('password');

    // 폼 제출 시 로딩 애니메이션
    loginForm.addEventListener('submit', function() {
        loginBtn.classList.add('btn-loading');
        loginBtn.innerHTML = '<span class="spinner"></span>로그인 중...';
        loginBtn.disabled = true;
    });

    // 엔터키로 폼 제출
    function handleEnterKey(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            loginForm.submit();
        }
    }

    adminIdInput.addEventListener('keypress', handleEnterKey);
    passwordInput.addEventListener('keypress', handleEnterKey);

    // 페이지 로드 시 첫 번째 입력 필드에 포커스
    setTimeout(() => {
        adminIdInput.focus();
    }, 500);

    // 에러 메시지가 있으면 해당 필드에 포커스
    const invalidInput = document.querySelector('.is-invalid');
    if (invalidInput) {
        setTimeout(() => {
            invalidInput.focus();
        }, 500);
    }

    // Caps Lock 감지
    function checkCapsLock(e) {
        const capsLockOn = e.getModifierState && e.getModifierState('CapsLock');
        const warning = document.getElementById('capsLockWarning');

        if (capsLockOn && e.target.type === 'password') {
            if (!warning) {
                const warningDiv = document.createElement('div');
                warningDiv.id = 'capsLockWarning';
                warningDiv.className = 'alert alert-warning mt-2 mb-0';
                warningDiv.style.fontSize = '0.8rem';
                warningDiv.innerHTML = '<i class="fas fa-exclamation-triangle me-1"></i>Caps Lock이 켜져 있습니다.';
                e.target.closest('.form-floating').appendChild(warningDiv);
            }
        } else if (warning) {
            warning.remove();
        }
    }

    passwordInput.addEventListener('keydown', checkCapsLock);
    passwordInput.addEventListener('keyup', checkCapsLock);
});
