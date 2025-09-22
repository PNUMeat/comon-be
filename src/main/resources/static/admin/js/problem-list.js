// problem-list.js

let allProblems = [];
let filteredProblems = [];
let currentPage = 1;
let itemsPerPage = 12;
let currentView = 'card';
let filters = {
    platform: 'ALL',
    problemStep: 'ALL',
    search: ''
};
let currentSort = 'newest';

document.addEventListener('DOMContentLoaded', function() {
    initializePage();
    setupEventListeners();
    loadProblems();
});

function initializePage() {
    updateActiveNavigation();
}

function setupEventListeners() {
    // 검색
    document.getElementById('searchInput').addEventListener('input', function() {
        filters.search = this.value.toLowerCase();
        applyFilters();
    });

    // 정렬
    document.getElementById('sortSelect').addEventListener('change', function() {
        currentSort = this.value;
        applyFilters();
    });

    // 플랫폼 필터
    document.querySelectorAll('.filter-btn[data-type="platform"]').forEach(btn => {
        btn.addEventListener('click', handleFilterClick);
    });

    // 뷰 토글
    document.querySelectorAll('.view-toggle').forEach(btn => {
        btn.addEventListener('click', function() {
            document.querySelectorAll('.view-toggle').forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            toggleView(this.dataset.view);
        });
    });
}

function updateActiveNavigation() {
    const currentPath = window.location.pathname;
    document.querySelectorAll('.sidebar .nav-link').forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });
}

function loadProblems() {
    showLoading();
    fetch('/admin/problems/problem-list')
        .then(r => r.json())
        .then(data => {
            if (data.status === 'success') {
                allProblems = data.data || [];
                initializeFilters();
                applyFilters();
            } else {
                throw new Error(data.message || '목록 불러오기 실패');
            }
        })
        .catch(err => {
            console.error('문제 목록 로드 실패:', err);
        })
        .finally(hideLoading);
}

function initializeFilters() {
    const steps = [...new Set(allProblems.map(p => p.problemStep).filter(s => s))];
    const stepFilters = document.getElementById('stepFilters');
    stepFilters.innerHTML = '<button class="filter-btn active" data-type="problemStep" data-value="ALL">전체</button>';
    steps.forEach(step => {
        const btn = document.createElement('button');
        btn.className = 'filter-btn';
        btn.dataset.type = 'problemStep';
        btn.dataset.value = step;
        btn.textContent = step;
        stepFilters.appendChild(btn);
    });
    setupDynamicFilterListeners();
}

function setupDynamicFilterListeners() {
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.removeEventListener('click', handleFilterClick);
        btn.addEventListener('click', handleFilterClick);
    });
}

function handleFilterClick() {
    const type = this.dataset.type;
    const value = this.dataset.value;
    document.querySelectorAll(`[data-type="${type}"]`).forEach(b => b.classList.remove('active'));
    this.classList.add('active');
    filters[type] = value;
    applyFilters();
}

function applyFilters() {
    filteredProblems = allProblems.filter(p => {
        if (filters.platform !== 'ALL' && p.platform !== filters.platform) return false;
        if (filters.problemStep !== 'ALL' && p.problemStep !== filters.problemStep) return false;
        if (filters.search) {
            const s = filters.search;
            if (!p.title.toLowerCase().includes(s) &&
                !p.platformProblemId.toLowerCase().includes(s)) return false;
        }
        return true;
    });
    sortProblems();
    currentPage = 1;
    renderProblems();
    updatePagination();
    updateProblemCount();
}

function sortProblems() {
    switch (currentSort) {
        case 'newest':
            filteredProblems.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            break;
        case 'oldest':
            filteredProblems.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
            break;
        case 'title':
            filteredProblems.sort((a, b) => a.title.localeCompare(b.title));
            break;
        case 'platform':
            filteredProblems.sort((a, b) => a.platform.localeCompare(b.platform));
            break;
        case 'problemStep':
            filteredProblems.sort((a, b) => (a.problemStep || '').localeCompare(b.problemStep || ''));
            break;
    }
}

function renderProblems() {
    const start = (currentPage - 1) * itemsPerPage;
    const end = start + itemsPerPage;
    const list = filteredProblems.slice(start, end);

    if (currentView === 'card') renderCardView(list);
    else renderTableView(list);

    document.getElementById('emptyState').style.display = list.length === 0 ? 'block' : 'none';
}

function renderCardView(list) {
    const grid = document.getElementById('problemGrid');
    if (list.length === 0) {
        grid.innerHTML = allProblems.length === 0
            ? `<div class="col-12 text-center p-5">
                 <i class="fas fa-inbox fa-3x text-muted mb-3"></i>
                 <h5 class="text-muted">등록된 문제가 없습니다</h5>
                 <a href="/admin/problems/register" class="btn btn-primary">
                     <i class="fas fa-plus me-1"></i>문제 등록하기
                 </a>
               </div>`
            : '';
        return;
    }

    grid.innerHTML = list.map(p => `
        <div class="problem-card">
            <div class="problem-card-header">
                <span class="platform-badge ${p.platform}">${getPlatformName(p.platform)}</span>
                <span class="problem-number">#${p.platformProblemId}</span>
            </div>
            <h6 class="problem-title">${p.title}</h6>
            <div class="problem-meta">
                <span class="difficulty-badge">${p.problemStep || '미지정'}</span>
            </div>
            <div class="problem-actions">
                <button class="btn-action btn-link" onclick="openProblemUrl('${p.url || ''}')">
                    <i class="fas fa-external-link-alt"></i>
                </button>
                <button class="btn-action btn-edit" onclick="openEditModal(${p.problemId})">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn-action btn-delete" onclick="deleteProblem(${p.problemId}, '${p.title}')">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </div>
    `).join('');
}

function renderTableView(list) {
    const tbody = document.getElementById('problemTableBody');
    if (list.length === 0) {
        tbody.innerHTML = allProblems.length === 0
            ? `<tr><td colspan="6" class="text-center py-5">
                  <i class="fas fa-inbox fa-2x text-muted mb-3 d-block"></i>
                  <h6 class="text-muted">등록된 문제가 없습니다</h6>
                  <a href="/admin/problems/register" class="btn btn-sm btn-primary mt-2">
                      <i class="fas fa-plus me-1"></i>문제 등록하기
                  </a>
               </td></tr>`
            : `<tr><td colspan="6" class="text-center py-4 text-muted">검색 조건에 맞는 문제가 없습니다.</td></tr>`;
        return;
    }

    tbody.innerHTML = list.map(p => `
        <tr>
            <td><span class="platform-badge ${p.platform}">${getPlatformName(p.platform)}</span></td>
            <td><strong>${p.platformProblemId}</strong></td>
            <td>${p.title}</td>
            <td>${p.problemStep || '미지정'}</td>
            <td><a href="${p.url || '#'}" target="_blank">${p.url ? '바로가기' : '-'}</a></td>
            <td>
                <div class="d-flex gap-1">
                    <button class="btn btn-sm btn-outline-info" onclick="openProblemUrl('${p.url || ''}')" title="문제 보기">
                        <i class="fas fa-external-link-alt"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-warning" onclick="openEditModal(${p.problemId})" title="수정">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteProblem(${p.problemId}, '${p.title}')" title="삭제">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function toggleView(view) {
    currentView = view;
    document.getElementById('problemGrid').style.display = view === 'card' ? 'grid' : 'none';
    document.getElementById('problemTable').style.display = view === 'table' ? 'block' : 'none';
    renderProblems();
}

function updatePagination() {
    const total = Math.ceil(filteredProblems.length / itemsPerPage);
    const pagination = document.getElementById('pagination');
    if (total <= 1) {
        pagination.innerHTML = '';
        return;
    }
    let html = '';
    if (currentPage > 1) {
        html += `<li class="page-item"><a class="page-link" href="#" onclick="changePage(${currentPage - 1});return false;">이전</a></li>`;
    }
    for (let i = 1; i <= total; i++) {
        html += `<li class="page-item ${i === currentPage ? 'active' : ''}">
                   <a class="page-link" href="#" onclick="changePage(${i});return false;">${i}</a>
                 </li>`;
    }
    if (currentPage < total) {
        html += `<li class="page-item"><a class="page-link" href="#" onclick="changePage(${currentPage + 1});return false;">다음</a></li>`;
    }
    pagination.innerHTML = html;
}

function changePage(page) {
    currentPage = page;
    renderProblems();
    updatePagination();
}

function updateProblemCount() {
    document.getElementById('problemCount').textContent = `${filteredProblems.length}개`;
}

function getPlatformName(platform) {
    return {
        BAEKJOON: '백준',
        PROGRAMMERS: '프로그래머스',
        LEETCODE: '리트코드'
    }[platform] || platform;
}

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

function openProblemUrl(url) {
    if (url && url.trim()) {
        window.open(url, '_blank');
    } else {
        alert('URL이 설정되지 않았습니다.');
    }
}

// 문제 삭제 함수
function deleteProblem(problemId, title) {
    if (!confirm(`"${title}" 문제를 삭제하시겠습니까?`)) return;

    fetch(`/admin/problems/${problemId}`, {
        method: 'DELETE'
    })
        .then(r => r.json())
        .then(data => {
            if (data.status === 'success') {
                alert('문제가 삭제되었습니다.');
                allProblems = allProblems.filter(p => p.problemId !== problemId);
                applyFilters();
            } else {
                alert('삭제 실패: ' + (data.message || '알 수 없는 오류'));
            }
        })
        .catch(err => {
            console.error('문제 삭제 실패:', err);
            alert('삭제 요청에 실패했습니다.');
        });
}

// 문제 수정 모달 열기
function openEditModal(problemId) {
    const problem = allProblems.find(p => p.problemId === problemId);
    if (!problem) {
        alert("문제를 찾을 수 없습니다.");
        return;
    }

    document.getElementById('editProblemId').value = problem.problemId;
    document.getElementById('editPlatform').value = getPlatformName(problem.platform);
    document.getElementById('editPlatformProblemId').value = problem.platformProblemId;
    document.getElementById('editTitle').value = problem.title;
    document.getElementById('editProblemStep').value = problem.problemStep || 'STEP1';
    document.getElementById('editUrl').value = problem.url || '';

    const modal = new bootstrap.Modal(document.getElementById('editModal'));
    modal.show();
}

// 문제 저장 함수
function saveProblem() {
    const problemId = document.getElementById('editProblemId').value;
    const platform = document.getElementById('editPlatform').value.trim();
    const platformProblemId = document.getElementById('editPlatformProblemId').value.trim();
    const title = document.getElementById('editTitle').value.trim();
    const problemStep = document.getElementById('editProblemStep').value;
    const url = document.getElementById('editUrl').value.trim();

    if (!title) {
        alert("제목은 필수 입력 항목입니다.");
        return;
    }

    const updateData = {
        title: title,
        problemStep: problemStep,
        url: url
    };

    fetch(`/admin/problems/${problemId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(updateData)
    })
        .then(r => r.json())
        .then(data => {
            if (data.status === 'success') {
                alert("문제가 수정되었습니다.");
                const idx = allProblems.findIndex(p => p.problemId === parseInt(problemId));
                if (idx !== -1) {
                    allProblems[idx] = { ...allProblems[idx], ...updateData };
                }
                applyFilters();
                bootstrap.Modal.getInstance(document.getElementById('editModal')).hide();
            } else {
                alert("수정 실패: " + (data.message || '알 수 없는 오류'));
            }
        })
        .catch(err => {
            console.error("수정 실패:", err);
            alert("수정 요청 중 오류가 발생했습니다.");
        });
}

function refreshProblems() {
    filters = { platform: 'ALL', problemStep: 'ALL', search: '' };
    currentSort = 'newest';
    currentPage = 1;
    document.getElementById('searchInput').value = '';
    document.getElementById('sortSelect').value = 'newest';
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
        if (btn.dataset.value === 'ALL') btn.classList.add('active');
    });
    loadProblems();
}
