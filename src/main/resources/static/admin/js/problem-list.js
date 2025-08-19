// problem-list.js

// 전역 변수
let allProblems = [];
let filteredProblems = [];
let currentPage = 1;
let itemsPerPage = 12;
let currentView = 'card';
let filters = {
    platform: 'ALL',
    difficulty: 'ALL',
    tag: 'ALL',
    search: ''
};
let currentSort = 'newest';

/**
 * DOM 로드 완료 시 초기화
 */
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
    setupEventListeners();
    loadProblems();
});

/**
 * 페이지 초기화
 */
function initializePage() {
    updateActiveNavigation();
}

/**
 * 이벤트 리스너 설정
 */
function setupEventListeners() {
    // 검색 입력
    const searchInput = document.getElementById('searchInput');
    let searchTimeout;
    searchInput.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            filters.search = this.value.toLowerCase();
            applyFilters();
        }, 300);
    });

    // 정렬 선택
    document.getElementById('sortSelect').addEventListener('change', function() {
        currentSort = this.value;
        applyFilters();
    });

    // 정적 필터 버튼들 (플랫폼 - HTML에 이미 있는 것들)
    document.querySelectorAll('.filter-btn[data-type="platform"]').forEach(btn => {
        btn.addEventListener('click', handleFilterClick);
    });

    // 뷰 토글
    document.querySelectorAll('.view-toggle').forEach(btn => {
        btn.addEventListener('click', function() {
            const view = this.dataset.view;

            // 버튼 상태 변경
            document.querySelectorAll('.view-toggle').forEach(b =>
                b.classList.remove('active'));
            this.classList.add('active');

            // 뷰 변경
            toggleView(view);
        });
    });
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
 * 문제 목록 로드
 */
function loadProblems() {
    showLoading();

    // 실제 API 호출
    fetch('/admin/problems/api/list')
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('문제 목록 로드 성공:', data);
            allProblems = data;
            initializeFilters();
            applyFilters();
            hideLoading();
        })
        .catch(error => {
            console.error('문제 목록 로드 실패:', error);
            hideLoading();

            // 에러 메시지 표시
            const grid = document.getElementById('problemGrid');
            grid.innerHTML = `
                <div class="col-12 text-center p-5">
                    <i class="fas fa-exclamation-triangle fa-3x text-warning mb-3"></i>
                    <h5 class="text-muted">문제 목록을 불러올 수 없습니다</h5>
                    <p class="text-muted">서버 연결을 확인해주세요.</p>
                    <button class="btn btn-primary" onclick="loadProblems()">
                        <i class="fas fa-sync-alt me-1"></i>다시 시도
                    </button>
                </div>
            `;
        });
}

/**
 * 필터 옵션 초기화
 */
function initializeFilters() {
    // 난이도 필터 생성
    const difficulties = [...new Set(allProblems.map(p => p.difficulty).filter(d => d))];
    const difficultyFilters = document.getElementById('difficultyFilters');
    difficultyFilters.innerHTML = '<button class="filter-btn active" data-type="difficulty" data-value="ALL">전체</button>';

    difficulties.forEach(difficulty => {
        const btn = document.createElement('button');
        btn.className = 'filter-btn';
        btn.setAttribute('data-type', 'difficulty');
        btn.setAttribute('data-value', difficulty);
        btn.textContent = difficulty;
        difficultyFilters.appendChild(btn);
    });

    // 태그 필터 생성
    const allTags = allProblems.flatMap(p =>
        p.tags ? p.tags.split(',').map(tag => tag.trim()) : []
    );
    const uniqueTags = [...new Set(allTags)].slice(0, 20); // 최대 20개 태그만 표시

    const tagFilters = document.getElementById('tagFilters');
    tagFilters.innerHTML = '<button class="filter-btn active" data-type="tag" data-value="ALL">전체</button>';

    uniqueTags.forEach(tag => {
        const btn = document.createElement('button');
        btn.className = 'filter-btn';
        btn.setAttribute('data-type', 'tag');
        btn.setAttribute('data-value', tag);
        btn.textContent = tag;
        tagFilters.appendChild(btn);
    });

    // 동적으로 생성된 필터 버튼들에 이벤트 리스너 추가
    setupDynamicFilterListeners();
}

/**
 * 동적으로 생성된 필터 버튼들에 이벤트 리스너 설정
 */
function setupDynamicFilterListeners() {
    // 모든 필터 버튼에 이벤트 리스너 추가 (기존 + 새로 생성된 것들)
    document.querySelectorAll('.filter-btn').forEach(btn => {
        // 기존 리스너 제거 후 새로 추가 (중복 방지)
        btn.removeEventListener('click', handleFilterClick);
        btn.addEventListener('click', handleFilterClick);
    });
}

/**
 * 필터 버튼 클릭 처리
 */
function handleFilterClick() {
    const type = this.dataset.type;
    const value = this.dataset.value;

    // 같은 타입의 다른 버튼들 비활성화
    document.querySelectorAll(`[data-type="${type}"]`).forEach(b =>
        b.classList.remove('active'));

    // 현재 버튼 활성화
    this.classList.add('active');

    // 필터 적용
    filters[type] = value;
    applyFilters();
}

/**
 * 필터 및 정렬 적용
 */
function applyFilters() {
    filteredProblems = allProblems.filter(problem => {
        // 플랫폼 필터
        if (filters.platform !== 'ALL' && problem.platform !== filters.platform) {
            return false;
        }

        // 난이도 필터
        if (filters.difficulty !== 'ALL' && problem.difficulty !== filters.difficulty) {
            return false;
        }

        // 태그 필터
        if (filters.tag !== 'ALL') {
            const problemTags = problem.tags ? problem.tags.toLowerCase().split(',').map(t => t.trim()) : [];
            if (!problemTags.includes(filters.tag.toLowerCase())) {
                return false;
            }
        }

        // 검색 필터
        if (filters.search) {
            const searchTerm = filters.search.toLowerCase();
            const titleMatch = problem.title.toLowerCase().includes(searchTerm);
            const numberMatch = problem.platformProblemId.toLowerCase().includes(searchTerm);
            if (!titleMatch && !numberMatch) {
                return false;
            }
        }

        return true;
    });

    // 정렬 적용
    sortProblems();

    // 페이지 초기화 및 렌더링
    currentPage = 1;
    renderProblems();
    updatePagination();
    updateProblemCount();
}

/**
 * 문제 정렬
 */
function sortProblems() {
    switch (currentSort) {
        case 'newest':
            filteredProblems.sort((a, b) => new Date(b.createdAt || b.createdDate) - new Date(a.createdAt || a.createdDate));
            break;
        case 'oldest':
            filteredProblems.sort((a, b) => new Date(a.createdAt || a.createdDate) - new Date(b.createdAt || b.createdDate));
            break;
        case 'title':
            filteredProblems.sort((a, b) => a.title.localeCompare(b.title));
            break;
        case 'platform':
            filteredProblems.sort((a, b) => a.platform.localeCompare(b.platform));
            break;
        case 'difficulty':
            filteredProblems.sort((a, b) => (a.difficulty || '').localeCompare(b.difficulty || ''));
            break;
    }
}

/**
 * 문제 목록 렌더링
 */
function renderProblems() {
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const problemsToShow = filteredProblems.slice(startIndex, endIndex);

    if (currentView === 'card') {
        renderCardView(problemsToShow);
    } else {
        renderTableView(problemsToShow);
    }

    // 빈 결과 처리
    const emptyState = document.getElementById('emptyState');
    if (problemsToShow.length === 0 && allProblems.length > 0) {
        emptyState.style.display = 'block';
    } else {
        emptyState.style.display = 'none';
    }
}

/**
 * 카드 뷰 렌더링
 */
function renderCardView(problems) {
    const grid = document.getElementById('problemGrid');

    if (problems.length === 0) {
        if (allProblems.length === 0) {
            grid.innerHTML = `
                <div class="col-12 text-center p-5">
                    <i class="fas fa-inbox fa-3x text-muted mb-3"></i>
                    <h5 class="text-muted">등록된 문제가 없습니다</h5>
                    <p class="text-muted">문제 등록 페이지에서 새로운 문제를 등록해보세요.</p>
                    <a href="/admin/problems/register" class="btn btn-primary">
                        <i class="fas fa-plus me-1"></i>문제 등록하기
                    </a>
                </div>
            `;
        } else {
            grid.innerHTML = '';
        }
        return;
    }

    grid.innerHTML = problems.map(problem => {
        const tags = problem.tags ? problem.tags.split(',').map(tag => tag.trim()) : [];
        const tagsHtml = tags.slice(0, 4).map(tag =>
            `<span class="tag">${tag}</span>`
        ).join('');

        return `
            <div class="problem-card">
                <div class="problem-card-header">
                    <span class="platform-badge ${problem.platform}">${getPlatformName(problem.platform)}</span>
                    <span class="problem-number">#${problem.platformProblemId}</span>
                </div>
                <h6 class="problem-title">${problem.title}</h6>
                <div class="problem-meta">
                    <span class="difficulty-badge">${problem.difficulty || '미지정'}</span>
                </div>
                <div class="problem-tags">
                    ${tagsHtml}
                    ${tags.length > 4 ? '<span class="tag">+' + (tags.length - 4) + '</span>' : ''}
                </div>
                <div class="problem-actions">
                    <button class="btn-action btn-link" onclick="openProblemUrl('${problem.url || ''}')">
                        <i class="fas fa-external-link-alt"></i>
                    </button>
                    <button class="btn-action btn-edit" onclick="editProblem(${problem.problemId})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn-action btn-delete" onclick="deleteProblem(${problem.problemId})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

/**
 * 테이블 뷰 렌더링
 */
function renderTableView(problems) {
    const tbody = document.getElementById('problemTableBody');

    if (problems.length === 0) {
        if (allProblems.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center py-5">
                        <i class="fas fa-inbox fa-2x text-muted mb-3 d-block"></i>
                        <h6 class="text-muted">등록된 문제가 없습니다</h6>
                        <a href="/admin/problems/register" class="btn btn-sm btn-primary mt-2">
                            <i class="fas fa-plus me-1"></i>문제 등록하기
                        </a>
                    </td>
                </tr>
            `;
        } else {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">검색 조건에 맞는 문제가 없습니다.</td></tr>';
        }
        return;
    }

    tbody.innerHTML = problems.map(problem => {
        const tags = problem.tags ? problem.tags.split(',').map(tag => tag.trim()).slice(0, 3) : [];
        const tagsText = tags.length > 0 ? tags.join(', ') : '태그 없음';

        return `
            <tr>
                <td>
                    <span class="platform-badge ${problem.platform}">${getPlatformName(problem.platform)}</span>
                </td>
                <td><strong>${problem.platformProblemId}</strong></td>
                <td>${problem.title}</td>
                <td>${problem.difficulty || '미지정'}</td>
                <td class="text-muted small">${tagsText}</td>
                <td>
                    <div class="d-flex gap-1">
                        <button class="btn btn-sm btn-outline-info" onclick="openProblemUrl('${problem.url || ''}')" title="문제 보기">
                            <i class="fas fa-external-link-alt"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-warning" onclick="editProblem(${problem.problemId})" title="수정">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteProblem(${problem.problemId})" title="삭제">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

/**
 * 뷰 전환
 */
function toggleView(view) {
    currentView = view;

    const grid = document.getElementById('problemGrid');
    const table = document.getElementById('problemTable');

    if (view === 'card') {
        grid.style.display = 'grid';
        table.style.display = 'none';
    } else {
        grid.style.display = 'none';
        table.style.display = 'block';
    }

    renderProblems();
}

/**
 * 페이지네이션 업데이트
 */
function updatePagination() {
    const totalPages = Math.ceil(filteredProblems.length / itemsPerPage);
    const pagination = document.getElementById('pagination');

    if (totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }

    let paginationHtml = '';

    // 이전 페이지
    if (currentPage > 1) {
        paginationHtml += `
            <li class="page-item">
                <a class="page-link" href="#" onclick="changePage(${currentPage - 1})">이전</a>
            </li>
        `;
    }

    // 페이지 번호들
    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, currentPage + 2);

    for (let i = startPage; i <= endPage; i++) {
        paginationHtml += `
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#" onclick="changePage(${i})">${i}</a>
            </li>
        `;
    }

    // 다음 페이지
    if (currentPage < totalPages) {
        paginationHtml += `
            <li class="page-item">
                <a class="page-link" href="#" onclick="changePage(${currentPage + 1})">다음</a>
            </li>
        `;
    }

    pagination.innerHTML = paginationHtml;
}

/**
 * 페이지 변경
 */
function changePage(page) {
    currentPage = page;
    renderProblems();
    updatePagination();

    // 스크롤을 맨 위로
    document.querySelector('.main-content').scrollTop = 0;
}

/**
 * 문제 수 업데이트
 */
function updateProblemCount() {
    document.getElementById('problemCount').textContent = `${filteredProblems.length}개`;
}

/**
 * 플랫폼 이름 가져오기
 */
function getPlatformName(platform) {
    const names = {
        'BAEKJOON': '백준',
        'PROGRAMMERS': '프로그래머스',
        'LEETCODE': '리트코드'
    };
    return names[platform] || platform;
}

/**
 * 로딩 상태 표시/숨김
 */
function showLoading() {
    document.getElementById('loadingContainer').style.display = 'block';
    document.getElementById('problemGrid').style.display = 'none';
    document.getElementById('problemTable').style.display = 'none';
    document.getElementById('emptyState').style.display = 'none';
}

function hideLoading() {
    document.getElementById('loadingContainer').style.display = 'none';
    toggleView(currentView);
}

/**
 * 문제 관리 함수들
 */
function openProblemUrl(url) {
    if (url && url.trim()) {
        window.open(url, '_blank');
    } else {
        alert('URL이 설정되지 않았습니다.');
    }
}

function editProblem(problemId) {
    const problem = allProblems.find(p => p.problemId == problemId);
    if (!problem) {
        alert('문제를 찾을 수 없습니다.');
        return;
    }

    // 모달에 데이터 설정
    document.getElementById('editProblemId').value = problem.problemId;
    document.getElementById('editPlatform').value = getPlatformName(problem.platform);
    document.getElementById('editPlatformProblemId').value = problem.platformProblemId;
    document.getElementById('editTitle').value = problem.title;
    document.getElementById('editDifficulty').value = problem.difficulty || '';
    document.getElementById('editTags').value = problem.tags || '';
    document.getElementById('editUrl').value = problem.url || '';

    // 모달 표시
    const modal = new bootstrap.Modal(document.getElementById('editModal'));
    modal.show();
}

function saveProblem() {
    const problemId = document.getElementById('editProblemId').value;
    const title = document.getElementById('editTitle').value.trim();
    const difficulty = document.getElementById('editDifficulty').value.trim();
    const tags = document.getElementById('editTags').value.trim();
    const url = document.getElementById('editUrl').value.trim();

    if (!title) {
        alert('제목은 필수 입력 항목입니다.');
        return;
    }

    const updateData = {
        title: title,
        difficulty: difficulty,
        tags: tags,
        url: url
    };

    // API 호출
    fetch(`/admin/problems/api/${problemId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(updateData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            alert('문제가 성공적으로 수정되었습니다.');

            // 로컬 데이터 업데이트
            const problemIndex = allProblems.findIndex(p => p.problemId == problemId);
            if (problemIndex !== -1) {
                allProblems[problemIndex] = { ...allProblems[problemIndex], ...updateData };
            }

            // 필터 다시 적용
            applyFilters();

            // 모달 닫기
            bootstrap.Modal.getInstance(document.getElementById('editModal')).hide();
        })
        .catch(error => {
            console.error('문제 수정 실패:', error);
            alert('문제 수정에 실패했습니다.');
        });
}

function deleteProblem(problemId) {
    const problem = allProblems.find(p => p.problemId == problemId);
    if (!problem) {
        alert('문제를 찾을 수 없습니다.');
        return;
    }

    if (!confirm(`"${problem.title}" 문제를 정말 삭제하시겠습니까?`)) {
        return;
    }

    // API 호출
    fetch(`/admin/problems/api/${problemId}`, {
        method: 'DELETE'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            alert('문제가 성공적으로 삭제되었습니다.');

            // 로컬 데이터에서 제거
            allProblems = allProblems.filter(p => p.problemId != problemId);

            // 필터 다시 적용
            applyFilters();
        })
        .catch(error => {
            console.error('문제 삭제 실패:', error);
            alert('문제 삭제에 실패했습니다.');
        });
}

/**
 * 문제 목록 새로고침
 */
function refreshProblems() {
    // 필터 초기화
    filters = {
        platform: 'ALL',
        difficulty: 'ALL',
        tag: 'ALL',
        search: ''
    };
    currentSort = 'newest';
    currentPage = 1;

    // UI 초기화
    document.getElementById('searchInput').value = '';
    document.getElementById('sortSelect').value = 'newest';

    // 모든 필터 버튼 비활성화 후 "전체" 버튼들만 활성화
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
        if (btn.dataset.value === 'ALL') {
            btn.classList.add('active');
        }
    });

    // 데이터 다시 로드
    loadProblems();
}

console.log('문제 목록 페이지 JavaScript 로드 완료');
