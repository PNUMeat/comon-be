// problem-register.js - 문제 등록 페이지 JavaScript

// 전역 변수
let currentPlatform = null;
let problemList = [];
let problemIdCounter = 0;

/**
 * DOM 로드 완료 시 초기화
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('문제 등록 페이지 초기화');
    updateActiveNavigation();
    setupEventListeners();
});

/**
 * 플랫폼 클릭 핸들러
 */
function handlePlatformClick(element) {
    console.log('플랫폼 선택:', element.dataset.platform);

    // 모든 카드와 폼 섹션 비활성화
    document.querySelectorAll('.platform-card').forEach(c => c.classList.remove('active'));
    document.querySelectorAll('.form-section').forEach(s => s.classList.remove('active'));

    // 선택된 카드 활성화
    element.classList.add('active');
    currentPlatform = element.dataset.platform;

    // 해당 폼 섹션 활성화
    const formId = currentPlatform.toLowerCase() + '-form';
    const formElement = document.getElementById(formId);

    if (formElement) {
        formElement.classList.add('active');
        console.log('폼 활성화:', formId);
    } else {
        console.error('폼을 찾을 수 없음:', formId);
    }

    clearInputs();
}

/**
 * 이벤트 리스너 설정
 */
function setupEventListeners() {
    // Enter 키 이벤트
    const baekjoonInput = document.getElementById('baekjoon-problem-id');
    const leetcodeInput = document.getElementById('leetcode-url');

    if (baekjoonInput) {
        baekjoonInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') checkBaekjoon();
        });
    }

    if (leetcodeInput) {
        leetcodeInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') checkLeetcode();
        });
    }
}

/**
 * 활성 네비게이션 업데이트
 */
function updateActiveNavigation() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.sidebar .nav-link');

    navLinks.forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });
}

/**
 * 입력 필드 초기화
 */
function clearInputs() {
    // 백준 입력 초기화
    const baekjoonInput = document.getElementById('baekjoon-problem-id');
    if (baekjoonInput) baekjoonInput.value = '';

    // 프로그래머스 입력 초기화
    clearProgrammersInputs();

    // 리트코드 입력 초기화
    const leetcodeInput = document.getElementById('leetcode-url');
    if (leetcodeInput) leetcodeInput.value = '';
}

function clearProgrammersInputs() {
    const inputs = [
        'programmers-problem-id',
        'programmers-title',
        'programmers-difficulty',
        'programmers-tags'
    ];

    inputs.forEach(id => {
        const element = document.getElementById(id);
        if (element) element.value = '';
    });
}

// ==================== 플랫폼별 문제 확인 함수들 ====================

/**
 * 백준 문제 확인 및 추가
 */
function checkBaekjoon() {
    const problemId = document.getElementById('baekjoon-problem-id').value.trim();

    if (!problemId) {
        alert('문제번호를 입력해주세요.');
        return;
    }

    if (!/^\d+$/.test(problemId) || parseInt(problemId) <= 0) {
        alert('유효한 문제번호를 입력해주세요.');
        return;
    }

    console.log('백준 문제 확인:', problemId);
    showLoading('백준 문제 정보를 가져오는 중...');

    fetch('/admin/problems/check/baekjoon', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'problemId=' + encodeURIComponent(problemId)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            hideLoading();
            handleProblemCheckResponse(data, 'baekjoon-problem-id');
        })
        .catch(error => {
            console.error('백준 문제 확인 실패:', error);
            hideLoading();
            alert('문제 정보를 가져오는데 실패했습니다: ' + error.message);
        });
}

/**
 * 프로그래머스 문제 확인 및 추가 (수동 입력)
 */
function checkProgrammers() {
    const data = {
        platformProblemId: document.getElementById('programmers-problem-id').value.trim(),
        title: document.getElementById('programmers-title').value.trim(),
        difficulty: document.getElementById('programmers-difficulty').value,
        tags: document.getElementById('programmers-tags').value.trim()
    };

    if (!data.platformProblemId || !data.title || !data.difficulty) {
        alert('모든 필수 정보를 입력해주세요.');
        return;
    }

    console.log('프로그래머스 문제 확인:', data);
    showLoading('프로그래머스 문제를 처리하는 중...');

    fetch('/admin/problems/check/programmers', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            hideLoading();
            if (data.success) {
                if (data.isDuplicate) {
                    alert(`이미 등록된 문제입니다: ${data.platform} ${data.platformProblemId}`);
                    clearProgrammersInputs();
                } else {
                    addProblemToList(data);
                    clearProgrammersInputs();
                    alert('프로그래머스 문제가 성공적으로 추가되었습니다.');
                }
            } else {
                alert('오류: ' + (data.errorMessage || '알 수 없는 오류'));
            }
        })
        .catch(error => {
            console.error('프로그래머스 문제 확인 실패:', error);
            hideLoading();
            alert('문제 정보를 처리하는데 실패했습니다: ' + error.message);
        });
}

/**
 * 리트코드 문제 확인 및 추가
 */
function checkLeetcode() {
    const url = document.getElementById('leetcode-url').value.trim();

    if (!url) {
        alert('LeetCode 문제 URL을 입력해주세요.');
        return;
    }

    if (!url.startsWith('https://leetcode.com/problems/')) {
        alert('올바른 LeetCode 문제 URL을 입력해주세요.\n예: https://leetcode.com/problems/merge-strings-alternately/');
        return;
    }

    console.log('리트코드 문제 확인:', url);
    showLoading('리트코드 문제 정보를 가져오는 중...');

    fetch('/admin/problems/check/leetcode', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `url=${encodeURIComponent(url)}`
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            hideLoading();
            console.log('리트코드 응답:', data);

            if (data.success) {
                if (data.isDuplicate) {
                    // 중복된 문제인 경우
                    alert(`이미 등록된 문제입니다: ${data.platform} ${data.platformProblemId}`);
                    document.getElementById('leetcode-url').value = '';
                } else {
                    // 새로운 문제인 경우
                    addProblemToList(data);
                    document.getElementById('leetcode-url').value = '';
                    alert(`문제가 성공적으로 추가되었습니다: ${data.title}`);
                }
            } else {
                alert('오류: ' + (data.errorMessage || '알 수 없는 오류'));
            }
        })
        .catch(error => {
            console.error('리트코드 문제 확인 실패:', error);
            hideLoading();
            alert('문제 정보를 가져오는데 실패했습니다: ' + error.message);
        });
}

/**
 * 문제 확인 응답 처리 (공통 함수)
 */
function handleProblemCheckResponse(data, inputId) {
    if (data.success) {
        if (data.isDuplicate) {
            alert(`이미 등록된 문제입니다: ${data.platform} ${data.platformProblemId}`);
            document.getElementById(inputId).value = '';
        } else {
            addProblemToList(data);
            document.getElementById(inputId).value = '';
            alert(`문제가 성공적으로 추가되었습니다: ${data.title}`);
        }
    } else {
        alert('오류: ' + (data.errorMessage || '알 수 없는 오류'));
    }
}

// ==================== 문제 목록 관리 함수들 ====================

/**
 * 문제 목록에 추가
 */
function addProblemToList(problemData) {
    // 서버에서 중복으로 표시된 문제는 추가하지 않음
    if (problemData.isDuplicate) {
        console.log('중복된 문제이므로 목록에 추가하지 않습니다:', problemData);
        return;
    }

    // 로컬 목록에서도 중복 체크
    const exists = problemList.some(p =>
        p.platform === problemData.platform &&
        p.platformProblemId === problemData.platformProblemId
    );

    if (exists) {
        alert('이미 목록에 추가된 문제입니다.');
        return;
    }

    problemData.id = ++problemIdCounter;
    problemList.push(problemData);
    renderProblemList();
    showProblemListCard();

    console.log('문제 추가됨:', problemData);
}

/**
 * 문제 목록 렌더링
 */
function renderProblemList() {
    const container = document.getElementById('problem-list');
    const countBadge = document.getElementById('problem-count');

    if (problemList.length === 0) {
        container.innerHTML = '<div class="text-center p-4 text-muted">등록할 문제가 없습니다.</div>';
        countBadge.textContent = '0개';
        return;
    }

    container.innerHTML = problemList.map(problem => {
        const statusClass = problem.isDuplicate ? 'status-duplicate' : 'status-success';
        const statusIcon = problem.isDuplicate ? 'fas fa-times-circle' : 'fas fa-check-circle';
        const statusText = problem.isDuplicate ? '중복됨' : '확인';

        return `
            <div class="problem-item d-flex justify-content-between align-items-center">
                <div class="flex-grow-1">
                    <div class="d-flex align-items-center mb-1">
                        <span class="badge bg-primary me-2">${problem.platform}</span>
                        <strong>${problem.platformProblemId}</strong>
                        <span class="ms-2">${problem.title}</span>
                        <span class="${statusClass} ms-2">
                            <i class="${statusIcon}"></i> ${statusText}
                        </span>
                    </div>
                    <small class="text-muted">
                        ${problem.difficulty || '난이도 미지정'} | ${problem.tags || '태그 없음'}
                    </small>
                </div>
                <button type="button" class="btn btn-sm btn-outline-danger"
                        onclick="removeProblem(${problem.id})">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        `;
    }).join('');

    countBadge.textContent = `${problemList.length}개`;
}

/**
 * 문제 제거
 */
function removeProblem(id) {
    if (!confirm('이 문제를 목록에서 제거하시겠습니까?')) {
        return;
    }

    problemList = problemList.filter(p => p.id !== id);
    renderProblemList();

    if (problemList.length === 0) {
        hideProblemListCard();
    }

    alert('문제가 목록에서 제거되었습니다.');
}

/**
 * 문제 목록 카드 표시/숨김
 */
function showProblemListCard() {
    const problemListCard = document.getElementById('problem-list-card');
    const actionButtons = document.getElementById('action-buttons');

    if (problemListCard) problemListCard.style.display = 'block';
    if (actionButtons) actionButtons.style.display = 'block';
}

function hideProblemListCard() {
    const problemListCard = document.getElementById('problem-list-card');
    const actionButtons = document.getElementById('action-buttons');

    if (problemListCard) problemListCard.style.display = 'none';
    if (actionButtons) actionButtons.style.display = 'none';
}

// ==================== 등록 및 초기화 함수들 ====================

/**
 * 모든 문제 등록
 */
function registerAllProblems() {
    const validProblems = problemList.filter(p => !p.isDuplicate);

    if (validProblems.length === 0) {
        alert('등록할 수 있는 문제가 없습니다.');
        return;
    }

    if (!confirm(`${validProblems.length}개의 문제를 등록하시겠습니까?`)) {
        return;
    }

    const requestData = {
        problems: validProblems.map(p => ({
            platform: p.platform,
            platformProblemId: p.platformProblemId,
            title: p.title,
            difficulty: p.difficulty,
            url: p.url,
            tags: p.tags || ''
        }))
    };

    console.log('등록할 데이터:', requestData);
    showLoading('문제들을 등록하는 중...');

    // 등록 버튼 비활성화
    const registerBtn = document.getElementById('register-btn');
    const originalContent = registerBtn ? registerBtn.innerHTML : '';
    if (registerBtn) {
        registerBtn.disabled = true;
        registerBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>등록 중...';
    }

    fetch('/admin/problems/register/batch', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            hideLoading();
            console.log('등록 결과:', data);

            if (data.success) {
                alert(`${data.count || validProblems.length}개 문제가 성공적으로 등록되었습니다!`);
                // 성공시 목록 비우기
                problemList = [];
                problemIdCounter = 0;
                renderProblemList();
                hideProblemListCard();
            } else {
                alert('문제 등록에 실패했습니다: ' + (data.message || '알 수 없는 오류'));
            }
        })
        .catch(error => {
            console.error('등록 에러:', error);
            hideLoading();
            alert('문제 등록 중 오류가 발생했습니다: ' + error.message);
        })
        .finally(() => {
            // 버튼 복원
            if (registerBtn) {
                registerBtn.disabled = false;
                registerBtn.innerHTML = originalContent || '<i class="fas fa-save me-2"></i>모든 문제 등록하기';
            }
        });
}

/**
 * 모든 문제 목록 비우기
 */
function clearAllProblems() {
    if (problemList.length > 0 && !confirm('모든 문제를 목록에서 제거하시겠습니까?')) {
        return;
    }

    problemList = [];
    problemIdCounter = 0;
    renderProblemList();
    hideProblemListCard();

    alert('문제 목록이 비워졌습니다.');
}

// ==================== 유틸리티 함수들 ====================

/**
 * 로딩 표시
 */
function showLoading(message = '처리 중...') {
    // 기존 로딩 오버레이 제거
    hideLoading();

    const loadingOverlay = document.createElement('div');
    loadingOverlay.id = 'loading-overlay';
    loadingOverlay.className = 'position-fixed top-0 start-0 w-100 h-100 d-flex justify-content-center align-items-center';
    loadingOverlay.style.backgroundColor = 'rgba(0,0,0,0.5)';
    loadingOverlay.style.zIndex = '9998';
    loadingOverlay.innerHTML = `
        <div class="bg-white p-4 rounded shadow">
            <div class="text-center">
                <div class="spinner-border text-primary me-2" role="status"></div>
                <span>${message}</span>
            </div>
        </div>
    `;
    document.body.appendChild(loadingOverlay);

    console.log('로딩 표시:', message);
}

/**
 * 로딩 숨김
 */
function hideLoading() {
    const loadingOverlay = document.getElementById('loading-overlay');
    if (loadingOverlay) {
        loadingOverlay.remove();
        console.log('로딩 숨김');
    }
}

console.log('문제 등록 JavaScript 파일이 성공적으로 로드되었습니다!');
