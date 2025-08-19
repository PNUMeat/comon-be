// dashboard.js

/**
 * DOM 로드 완료 시 초기화
 */
document.addEventListener('DOMContentLoaded', function() {
    initializeDashboard();
    setupEventListeners();
});

/**
 * 대시보드 초기화
 */
function initializeDashboard() {
    updateActiveNavigation();
    animateCounters();
    animateProgressBars();
}

/**
 * 이벤트 리스너 설정
 */
function setupEventListeners() {
    // 새로고침 버튼 이벤트는 HTML에서 직접 처리
    // 필요시 추가 이벤트 리스너 설정
}

/**
 * 활성 네비게이션 업데이트
 */
function updateActiveNavigation() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.sidebar .nav-link');

    navLinks.forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('href') === currentPath ||
            (currentPath === '/admin/problems' && link.getAttribute('href') === '/admin/problems/dashboard')) {
            link.classList.add('active');
        }
    });
}

/**
 * 카운터 애니메이션
 */
function animateCounters() {
    const counters = document.querySelectorAll('.stat-info h2');

    counters.forEach(counter => {
        const target = parseInt(counter.textContent);
        if (isNaN(target)) return;

        let current = 0;
        const increment = target / 30; // 30프레임에 걸쳐 애니메이션

        const updateCounter = () => {
            if (current < target) {
                current += increment;
                counter.textContent = Math.ceil(current);
                requestAnimationFrame(updateCounter);
            } else {
                counter.textContent = target;
            }
        };

        // 페이지 로드 후 약간의 딜레이를 두고 시작
        setTimeout(updateCounter, 200);
    });
}

/**
 * 프로그레스 바 애니메이션
 */
function animateProgressBars() {
    const progressBars = document.querySelectorAll('.progress-bar');

    progressBars.forEach(bar => {
        const targetWidth = bar.style.width;
        bar.style.width = '0%';

        setTimeout(() => {
            bar.style.width = targetWidth;
        }, 500);
    });
}

/**
 * 통계 새로고침
 */
function refreshStats() {
    // 로딩 상태 표시
    showLoading();

    // 페이지 새로고침
    setTimeout(() => {
        location.reload();
    }, 500);
}

/**
 * 로딩 표시
 */
function showLoading() {
    const refreshBtn = document.querySelector('.action-btn.warning');
    if (refreshBtn) {
        const originalContent = refreshBtn.innerHTML;
        refreshBtn.innerHTML = `
            <div class="action-icon">
                <i class="fas fa-spinner fa-spin"></i>
            </div>
            <div class="action-content">
                <h6>새로고침 중...</h6>
                <p>데이터를 업데이트하고 있습니다</p>
            </div>
        `;
        refreshBtn.disabled = true;
    }
}

/**
 * 플랫폼별 세부 정보 토글
 */
function togglePlatformDetails(platform) {
    const detailCard = document.getElementById(`${platform}-details`);
    if (detailCard) {
        detailCard.classList.toggle('show');
    }
}

/**
 * 통계 데이터 포맷팅
 */
function formatNumber(num) {
    if (num >= 1000000) {
        return (num / 1000000).toFixed(1) + 'M';
    }
    if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
}

/**
 * 퍼센트 계산
 */
function calculatePercentage(value, total) {
    if (total === 0) return 0;
    return Math.round((value / total) * 100 * 10) / 10;
}

/**
 * 카드 호버 효과 강화
 */
function enhanceCardEffects() {
    const statCards = document.querySelectorAll('.stat-card');

    statCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-8px)';
        });

        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
}

/**
 * 실시간 시간 업데이트
 */
function updateCurrentTime() {
    const timeElement = document.querySelector('[th\\:text*="temporals.format"]');
    if (timeElement) {
        const now = new Date();
        const formattedTime = now.toLocaleString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });

        // Thymeleaf가 처리하지 않은 경우에만 업데이트
        if (timeElement.textContent === '') {
            timeElement.textContent = formattedTime;
        }
    }
}

/**
 * 키보드 단축키 설정
 */
function setupKeyboardShortcuts() {
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + R: 새로고침
        if ((e.ctrlKey || e.metaKey) && e.key === 'r') {
            e.preventDefault();
            refreshStats();
        }

        // Ctrl/Cmd + N: 새 문제 등록
        if ((e.ctrlKey || e.metaKey) && e.key === 'n') {
            e.preventDefault();
            window.location.href = '/admin/problems/register';
        }

        // Ctrl/Cmd + L: 문제 목록
        if ((e.ctrlKey || e.metaKey) && e.key === 'l') {
            e.preventDefault();
            window.location.href = '/admin/problems/list';
        }
    });
}

/**
 * 툴팁 초기화
 */
function initializeTooltips() {
    // Bootstrap 툴팁이 있다면 초기화
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
        tooltipTriggerList.map(function(tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }
}

/**
 * 페이지 성능 모니터링
 */
function monitorPagePerformance() {
    if ('performance' in window) {
        window.addEventListener('load', function() {
            setTimeout(function() {
                const perfData = performance.getEntriesByType('navigation')[0];
                const loadTime = perfData.loadEventEnd - perfData.loadEventStart;

                console.log(`대시보드 로드 시간: ${loadTime}ms`);

                // 로드 시간이 너무 길면 경고
                if (loadTime > 3000) {
                    console.warn('대시보드 로드 시간이 길어요. 성능 최적화를 고려해보세요.');
                }
            }, 0);
        });
    }
}

/**
 * 에러 처리
 */
function handleErrors() {
    window.addEventListener('error', function(e) {
        console.error('대시보드 에러:', e.error);

        // 사용자에게 친화적인 에러 메시지 표시
        showErrorMessage('일시적인 오류가 발생했습니다. 페이지를 새로고침해 주세요.');
    });
}

/**
 * 에러 메시지 표시
 */
function showErrorMessage(message) {
    // 간단한 에러 알림 (Toast나 다른 방식으로 대체 가능)
    const errorDiv = document.createElement('div');
    errorDiv.className = 'alert alert-warning alert-dismissible fade show position-fixed';
    errorDiv.style.cssText = 'top: 20px; right: 20px; z-index: 9999; max-width: 400px;';
    errorDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    document.body.appendChild(errorDiv);

    // 5초 후 자동 제거
    setTimeout(() => {
        if (errorDiv.parentNode) {
            errorDiv.remove();
        }
    }, 5000);
}

/**
 * 초기화 함수 업데이트
 */
function initializeDashboard() {
    updateActiveNavigation();
    animateCounters();
    animateProgressBars();
    enhanceCardEffects();
    updateCurrentTime();
    setupKeyboardShortcuts();
    initializeTooltips();
    monitorPagePerformance();
    handleErrors();
}
