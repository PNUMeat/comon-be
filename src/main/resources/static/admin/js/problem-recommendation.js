// problem-recommendation.js - 문제 추천 설정 페이지 JavaScript

// 전역 변수
let currentTeamId = null;
let selectedDates = [];
let platformSettings = {
    BAEKJOON: { enabled: false, difficulties: [], tags: [], problemCount: 2 },
    PROGRAMMERS: { enabled: false, difficulties: [], tags: [], problemCount: 2 },
    LEETCODE: { enabled: false, difficulties: [], tags: [], problemCount: 2 }
};
let autoRecommendationEnabled = false;
let selectedDays = [];

/**
 * DOM 로드 완료 시 초기화
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('문제 추천 설정 페이지 초기화');
    updateActiveNavigation();
    initializeDatePicker();
    loadTeamOptions(); // 이 부분이 핵심
});

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
 * 날짜 선택기 초기화
 */
function initializeDatePicker() {
    const datePicker = document.getElementById('manual-date-picker');
    if (datePicker) {
        // 오늘 날짜를 최소값으로 설정
        const today = new Date().toISOString().split('T')[0];
        datePicker.min = today;
    }
}

/**
 * 팀 옵션 로드
 */
function loadTeamOptions() {
    fetch('/api/v1/teams/all')
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            // data.status를 사용하여 성공 여부를 명확히 판단
            if (data.status === 'success') {
                populateTeamOptions(data.data);
            } else {
                console.error('팀 목록 로드 실패:', data.message);
            }
        })
        .catch(error => {
            console.error('팀 목록 로드 중 오류:', error);
        });
}

/**
 * 팀 선택 옵션 채우기
 */
function populateTeamOptions(teams) {
    const teamSelect = document.getElementById('team-select');

    // 기존 옵션 제거 (첫 번째 "팀을 선택하세요" 옵션 제외)
    while (teamSelect.children.length > 1) {
        teamSelect.removeChild(teamSelect.lastChild);
    }

    // 새 팀 옵션 추가
    teams.forEach(team => {
        const option = document.createElement('option');
        option.value = team.teamId;
        option.textContent = `${team.teamName} (${team.memberCount}명)`;
        teamSelect.appendChild(option);
    });
}

// ==================== 팀 관련 함수들 ====================

/**
 * 팀 선택 변경 핸들러
 */
function handleTeamChange() {
    const teamSelect = document.getElementById('team-select');
    const selectedTeamId = teamSelect.value;

    if (selectedTeamId) {
        currentTeamId = selectedTeamId;
        enableTeamDependentElements();
        showSettingsCards();

        // ★★★ 순서 변경: 플랫폼 옵션 먼저 로드하고, 완료 후 팀 설정 적용 ★★★
        loadPlatformOptions().then(() => {
            // 옵션 로드 완료 후 팀 설정 적용
            loadTeamSettings();
        });
    } else {
        currentTeamId = null;
        disableTeamDependentElements();
        hideSettingsCards();
    }
}

/**
 * 팀 의존적 요소들 활성화
 */
function enableTeamDependentElements() {
    document.getElementById('load-btn').disabled = false;
}

/**
 * 팀 의존적 요소들 비활성화
 */
function disableTeamDependentElements() {
    document.getElementById('load-btn').disabled = true;
}

/**
 * 설정 카드들 표시
 */
function showSettingsCards() {
    document.getElementById('platform-settings-card').style.display = 'block';
    document.getElementById('schedule-settings-card').style.display = 'block';
    document.getElementById('save-buttons').style.display = 'block';
    document.getElementById('manual-recommendation-card').style.display = 'block';

    // 애니메이션 효과
    setTimeout(() => {
        document.getElementById('platform-settings-card').classList.add('fade-in');
        document.getElementById('schedule-settings-card').classList.add('fade-in');
        document.getElementById('manual-recommendation-card').classList.add('fade-in');
    }, 100);
}

/**
 * 설정 카드들 숨김
 */
function hideSettingsCards() {
    const cards = ['platform-settings-card', 'schedule-settings-card', 'manual-recommendation-card', 'save-buttons'];
    cards.forEach(cardId => {
        const card = document.getElementById(cardId);
        if (card) {
            card.style.display = 'none';
            card.classList.remove('fade-in');
        }
    });
}

/**
 * 팀 설정 불러오기
 */
function loadTeamSettings() {
    if (!currentTeamId) {
        alert('팀을 먼저 선택해주세요.');
        return;
    }

    showLoading('팀 설정을 불러오는 중...');

    fetch(`/admin/recommendations/settings/${currentTeamId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            hideLoading();
            if (data.status === 'success') {
                applyTeamSettings(data.data);
            } else {
                alert('설정을 불러오는데 실패했습니다: ' + (data.message || '알 수 없는 오류'));
            }
        })
        .catch(error => {
            console.error('팀 설정 불러오기 실패:', error);
            hideLoading();
            alert('설정을 불러오는데 실패했습니다: ' + error.message);
        });
}

/**
 * 불러온 팀 설정 적용
 */
/**
 * 불러온 팀 설정 적용
 */
function applyTeamSettings(settings) {
    // 자동 추천 설정
    const autoToggle = document.getElementById('auto-recommendation-enabled');
    if (autoToggle && settings.autoRecommendationEnabled !== undefined) {
        autoRecommendationEnabled = settings.autoRecommendationEnabled;
        if (autoRecommendationEnabled) {
            autoToggle.classList.add('active');
        } else {
            autoToggle.classList.remove('active');
        }
        toggleScheduleSettings();
    }

    // 추천 시간 설정
    const timeSelect = document.getElementById('recommendation-time');
    if (timeSelect && settings.recommendationAt !== undefined) {
        timeSelect.value = settings.recommendationAt;
    }

    // 요일 설정
    if (settings.recommendDays) {
        selectedDays = settings.recommendDays;
        document.querySelectorAll('.day-btn').forEach(btn => {
            const day = btn.getAttribute('data-day');
            if (selectedDays.includes(day)) {
                btn.classList.add('active');
            } else {
                btn.classList.remove('active');
            }
        });
    }

    // 플랫폼 설정 초기화
    Object.keys(platformSettings).forEach(platform => {
        platformSettings[platform] = { enabled: false, difficulties: [], tags: [], problemCount: 2 };
    });

    document.querySelectorAll('.platform-toggle-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.platform-card').forEach(card => card.classList.remove('enabled'));
    document.querySelectorAll('.platform-setting').forEach(setting => setting.style.display = 'none');

    if (settings.platformSettings) {
        settings.platformSettings.forEach(loadedSetting => {
            const platform = loadedSetting.platform.toUpperCase();
            const platformLower = platform.toLowerCase();
            const isEnabled = loadedSetting.enabled;

            // 전역 상태 업데이트
            platformSettings[platform].enabled = isEnabled;
            platformSettings[platform].difficulties = loadedSetting.difficulties || [];
            platformSettings[platform].tags = loadedSetting.tags || [];
            platformSettings[platform].problemCount = loadedSetting.problemCount || 2;

            if (isEnabled) {
                const card = document.querySelector(`.platform-card[data-platform="${platform}"]`);
                const toggleBtn = document.getElementById(`toggle-${platformLower}`);
                const countInput = document.getElementById(`${platformLower}-count`);
                const difficultyContainer = document.getElementById(`${platformLower}-difficulty-container`);
                const tagsContainer = document.getElementById(`${platformLower}-tags-container`);

                if (card) card.classList.add('enabled');
                if (toggleBtn) toggleBtn.classList.add('active');
                if (countInput) countInput.value = loadedSetting.problemCount || 2;

                // 체크박스 선택 적용 부분
                if (difficultyContainer && loadedSetting.difficulties && loadedSetting.difficulties.length > 0) {
                    loadedSetting.difficulties.forEach(difficulty => {
                        const checkboxDiv = difficultyContainer.querySelector(`input[value="${difficulty}"]`)?.closest('.form-check');
                        if (checkboxDiv) {
                            const checkbox = checkboxDiv.querySelector('input[type="checkbox"]');
                            checkbox.checked = true;
                            checkboxDiv.classList.add('selected');
                        }
                    });
                }

                if (tagsContainer && loadedSetting.tags && loadedSetting.tags.length > 0) {
                    loadedSetting.tags.forEach(tag => {
                        const checkboxDiv = tagsContainer.querySelector(`input[value="${tag}"]`)?.closest('.form-check');
                        if (checkboxDiv) {
                            const checkbox = checkboxDiv.querySelector('input[type="checkbox"]');
                            checkbox.checked = true;
                            checkboxDiv.classList.add('selected');
                        }
                    });
                }
            }
        });
        updatePlatformDetailsVisibility();
    }
}

// ==================== 플랫폼 관련 함수들 ====================

/**
 * 플랫폼 카드 클릭 핸들러
 */
function handlePlatformClick(element) {
    const platform = element.dataset.platform;
    console.log('플랫폼 선택:', platform);

    // 모든 카드 비활성화
    document.querySelectorAll('.platform-card').forEach(card => {
        card.classList.remove('active');
    });

    // 선택된 카드 활성화
    element.classList.add('active');

    // 해당 플랫폼 설정 표시
    showPlatformDetails(platform);
}

/**
 * 플랫폼 토글 버튼 핸들러
 */
function togglePlatform(event, buttonElement) {
    event.stopPropagation(); // 카드 클릭 이벤트 방지

    const platform = buttonElement.id.replace('toggle-', '').toUpperCase();
    const isEnabled = buttonElement.classList.contains('active');
    const platformCard = buttonElement.closest('.platform-card');

    console.log(`플랫폼 ${platform} ${isEnabled ? '비활성화' : '활성화'}`);

    // 버튼 상태 토글
    if (isEnabled) {
        buttonElement.classList.remove('active');
        platformCard.classList.remove('enabled');
    } else {
        buttonElement.classList.add('active');
        platformCard.classList.add('enabled');
    }

    // 플랫폼 설정 업데이트
    platformSettings[platform].enabled = !isEnabled;

    // 플랫폼 상세 설정 표시/숨김
    updatePlatformDetailsVisibility();
}

/**
 * 플랫폼 상세 설정 표시
 */
function showPlatformDetails(platform) {
    const detailsContainer = document.getElementById('platform-details');

    // 모든 플랫폼 설정 숨김
    document.querySelectorAll('.platform-setting').forEach(setting => {
        setting.style.display = 'none';
    });

    // 선택된 플랫폼 설정 표시
    const platformSettingId = platform.toLowerCase() + '-setting';
    const platformSetting = document.getElementById(platformSettingId);

    if (platformSetting) {
        platformSetting.style.display = 'block';
        detailsContainer.style.display = 'block';
    }
}

/**
 * 활성화된 플랫폼에 따른 상세 설정 표시 여부 업데이트
 */
function updatePlatformDetailsVisibility() {
    const hasEnabledPlatform = Object.values(platformSettings).some(setting => setting.enabled);
    const detailsContainer = document.getElementById('platform-details');

    if (hasEnabledPlatform) {
        detailsContainer.style.display = 'block';
    } else {
        detailsContainer.style.display = 'none';
    }
}

/**
 * 플랫폼 옵션 로드 (난이도, 태그) - Promise 반환
 */
function loadPlatformOptions() {
    const platforms = ['BAEKJOON', 'PROGRAMMERS', 'LEETCODE'];

    const promises = platforms.map(platform => loadPlatformData(platform));

    return Promise.all(promises);
}

/**
 * 특정 플랫폼의 문제 데이터를 로드하고 난이도/태그 옵션 생성 - Promise 반환
 */
function loadPlatformData(platform) {
    console.log(`${platform} 플랫폼 데이터 로드 중...`);

    return fetch(`/admin/problems/list-by-platform?platform=${platform}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log(`${platform} 플랫폼 데이터 로드 성공:`, data);
            if (data.status === 'success') {
                populatePlatformOptions(platform, data.data || []);
            } else {
                console.error(`${platform} 옵션 로드 실패:`, data.message);
                populateDefaultOptions(platform);
            }
        })
        .catch(error => {
            console.error(`${platform} 옵션 로드 실패:`, error);
            populateDefaultOptions(platform);
        });
}

/**
 * 플랫폼별 문제 데이터에서 난이도, 태그 옵션 생성
 */
function populatePlatformOptions(platform, problems) {
    const platformLower = platform.toLowerCase();

    // ★★★ 난이도 옵션 생성 (빠진 부분 추가) ★★★
    let difficulties = [];
    if (problems && problems.length > 0) {
        difficulties = [...new Set(problems
            .map(p => p.difficulty)
            .filter(d => d && d.trim())
            .map(d => d.trim())
        )].sort();
    }

    const difficultyContainer = document.getElementById(`${platformLower}-difficulty-container`);
    if (difficultyContainer) {
        difficultyContainer.innerHTML = '';
        if (difficulties.length > 0) {
            difficulties.forEach(difficulty => {
                const checkboxDiv = document.createElement('div');
                checkboxDiv.className = 'form-check';
                checkboxDiv.innerHTML = `
                    <input class="form-check-input" type="checkbox" 
                           value="${difficulty}" 
                           id="${platformLower}-diff-${difficulty.replace(/[^a-zA-Z0-9]/g, '')}">
                    <label class="form-check-label" 
                           for="${platformLower}-diff-${difficulty.replace(/[^a-zA-Z0-9]/g, '')}">
                        ${difficulty}
                    </label>
                `;

                // 칸 클릭 이벤트 추가
                checkboxDiv.addEventListener('click', function() {
                    const checkbox = this.querySelector('input[type="checkbox"]');
                    checkbox.checked = !checkbox.checked;

                    if (checkbox.checked) {
                        this.classList.add('selected');
                    } else {
                        this.classList.remove('selected');
                    }
                });

                difficultyContainer.appendChild(checkboxDiv);
            });
        } else {
            difficultyContainer.innerHTML = '<div class="text-muted text-center p-3">난이도 없음</div>';
        }
    }

    // ★★★ 태그 옵션 생성 (빠진 부분 추가) ★★★
    let allTags = [];
    if (problems && problems.length > 0) {
        allTags = problems
            .map(p => p.tags)
            .filter(t => t && t.trim())
            .flatMap(t => t.split(',').map(tag => tag.trim()))
            .filter(tag => tag.length > 0);
    }

    const uniqueTags = [...new Set(allTags)].sort();

    const tagsContainer = document.getElementById(`${platformLower}-tags-container`);
    if (tagsContainer) {
        tagsContainer.innerHTML = '';
        if (uniqueTags.length > 0) {
            uniqueTags.forEach(tag => {
                const checkboxDiv = document.createElement('div');
                checkboxDiv.className = 'form-check';
                checkboxDiv.innerHTML = `
                    <input class="form-check-input" type="checkbox" 
                           value="${tag}" 
                           id="${platformLower}-tag-${tag.replace(/[^a-zA-Z0-9]/g, '')}">
                    <label class="form-check-label" 
                           for="${platformLower}-tag-${tag.replace(/[^a-zA-Z0-9]/g, '')}">
                        ${tag}
                    </label>
                `;

                checkboxDiv.addEventListener('click', function() {
                    const checkbox = this.querySelector('input[type="checkbox"]');
                    checkbox.checked = !checkbox.checked;

                    if (checkbox.checked) {
                        this.classList.add('selected');
                    } else {
                        this.classList.remove('selected');
                    }
                });

                tagsContainer.appendChild(checkboxDiv);
            });
        } else {
            tagsContainer.innerHTML = '<div class="text-muted text-center p-3">태그 없음</div>';
        }
    }
}

/**
 * 기본 옵션 설정 (API 호출 실패 시)
 */
function populateDefaultOptions(platform) {
    const platformLower = platform.toLowerCase();

    // 플랫폼별 기본 난이도
    const defaultDifficulties = {
        'baekjoon': ['Bronze I', 'Bronze II', 'Bronze III', 'Bronze IV', 'Bronze V',
            'Silver I', 'Silver II', 'Silver III', 'Silver IV', 'Silver V',
            'Gold I', 'Gold II', 'Gold III', 'Gold IV', 'Gold V',
            'Platinum I', 'Platinum II', 'Platinum III', 'Platinum IV', 'Platinum V',
            'Diamond I', 'Diamond II', 'Diamond III', 'Diamond IV', 'Diamond V'],
        'programmers': ['Level 0', 'Level 1', 'Level 2', 'Level 3', 'Level 4', 'Level 5'],
        'leetcode': ['Easy', 'Medium', 'Hard']
    };

    const difficultySelect = document.getElementById(`${platformLower}-difficulty`);
    if (difficultySelect && defaultDifficulties[platformLower]) {
        difficultySelect.innerHTML = '';
        defaultDifficulties[platformLower].forEach(difficulty => {
            const option = document.createElement('option');
            option.value = difficulty;
            option.textContent = difficulty;
            difficultySelect.appendChild(option);
        });
    }

    // 태그는 빈 상태로 두기
    const tagsSelect = document.getElementById(`${platformLower}-tags`);
    if (tagsSelect) {
        tagsSelect.innerHTML = '<option value="" disabled>문제 데이터를 불러올 수 없습니다</option>';
    }

    console.log(`${platform} 기본 옵션 설정 완료`);
}

// ==================== 스케줄 관련 함수들 ====================

/**
 * 자동 추천 토글 버튼 핸들러
 */
function toggleAutoRecommendation(buttonElement) {
    const isActive = buttonElement.classList.contains('active');

    if (isActive) {
        buttonElement.classList.remove('active');
        autoRecommendationEnabled = false;
    } else {
        buttonElement.classList.add('active');
        autoRecommendationEnabled = true;
    }

    toggleScheduleSettings();
}

/**
 * 자동 추천 스케줄 설정 토글
 */
function toggleScheduleSettings() {
    const timeSettings = document.getElementById('schedule-time-settings');
    const daysSettings = document.getElementById('schedule-days-settings');

    if (autoRecommendationEnabled) {
        timeSettings.classList.remove('disabled');
        daysSettings.classList.remove('disabled');
    } else {
        timeSettings.classList.add('disabled');
        daysSettings.classList.add('disabled');
    }
}

/**
 * 요일 토글 버튼 핸들러
 */
function toggleDay(buttonElement) {
    const day = buttonElement.getAttribute('data-day');
    const isActive = buttonElement.classList.contains('active');

    if (isActive) {
        buttonElement.classList.remove('active');
        selectedDays = selectedDays.filter(d => d !== day);
    } else {
        buttonElement.classList.add('active');
        selectedDays.push(day);
    }
}

// ==================== 수동 추천 관련 함수들 ====================

/**
 * 선택된 날짜 추가
 */
function addSelectedDate() {
    const datePicker = document.getElementById('manual-date-picker');
    const selectedDate = datePicker.value;

    if (!selectedDate) {
        return;
    }

    // 중복 체크
    if (selectedDates.includes(selectedDate)) {
        alert('이미 선택된 날짜입니다.');
        return;
    }

    // 과거 날짜 체크
    const today = new Date().toISOString().split('T')[0];
    if (selectedDate < today) {
        alert('과거 날짜는 선택할 수 없습니다.');
        return;
    }

    selectedDates.push(selectedDate);
    updateSelectedDatesDisplay();
    datePicker.value = '';

    // 수동 추천 버튼 활성화
    document.getElementById('manual-recommend-btn').disabled = false;
}

/**
 * 선택된 날짜들 표시 업데이트
 */
function updateSelectedDatesDisplay() {
    const selectedDatesContainer = document.getElementById('selected-dates');
    const dateList = document.getElementById('date-list');

    if (selectedDates.length === 0) {
        selectedDatesContainer.style.display = 'none';
        return;
    }

    selectedDatesContainer.style.display = 'block';
    dateList.innerHTML = '';

    selectedDates.forEach(date => {
        const dateTag = document.createElement('span');
        dateTag.className = 'date-tag';
        dateTag.innerHTML = `
            ${formatDate(date)}
            <button type="button" class="remove-date" onclick="removeSelectedDate('${date}')">
                <i class="fas fa-times"></i>
            </button>
        `;
        dateList.appendChild(dateTag);
    });
}

/**
 * 선택된 날짜 제거
 */
function removeSelectedDate(dateToRemove) {
    selectedDates = selectedDates.filter(date => date !== dateToRemove);
    updateSelectedDatesDisplay();

    // 선택된 날짜가 없으면 수동 추천 버튼 비활성화
    if (selectedDates.length === 0) {
        document.getElementById('manual-recommend-btn').disabled = true;
    }
}

/**
 * 날짜 포맷팅 (YYYY-MM-DD → MM/DD)
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${month}/${day}`;
}

/**
 * 수동 추천 실행
 */
function executeManualRecommendation() {
    if (!currentTeamId) {
        alert('팀을 먼저 선택해주세요.');
        return;
    }

    if (selectedDates.length === 0) {
        alert('추천할 날짜를 선택해주세요.');
        return;
    }

    if (!confirm(`선택된 ${selectedDates.length}개 날짜에 문제를 추천하시겠습니까?`)) {
        return;
    }

    const requestData = {
        teamId: currentTeamId,
        selectedDates: selectedDates
    };

    showLoading('수동 추천을 실행하는 중...');

    fetch('/admin/recommendations/manual', {
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
            if (data.status === 'success') {
                alert(`${data.data.message || '수동 추천이 완료되었습니다'}`);
                // 선택된 날짜들 초기화
                selectedDates = [];
                updateSelectedDatesDisplay();
                document.getElementById('manual-recommend-btn').disabled = true;
            } else {
                alert('수동 추천에 실패했습니다: ' + (data.message || '알 수 없는 오류'));
            }
        })
        .catch(error => {
            console.error('수동 추천 실행 실패:', error);
            hideLoading();
            alert('수동 추천 실행 중 오류가 발생했습니다: ' + error.message);
        });
}

// ==================== 설정 저장/초기화 함수들 ====================

/**
 * 설정 저장
 */
function saveSettings() {
    if (!currentTeamId) {
        alert('팀을 먼저 선택해주세요.');
        return;
    }

    const settings = collectCurrentSettings();

    if (!validateSettings(settings)) {
        return;
    }

    if (!confirm('현재 설정을 저장하시겠습니까?')) {
        return;
    }

    showLoading('설정을 저장하는 중...');

    fetch('/admin/recommendations/settings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(settings)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            hideLoading();
            if (data.status === 'success') {
                alert('설정이 성공적으로 저장되었습니다!');
            } else {
                alert('설정 저장에 실패했습니다: ' + (data.message || '알 수 없는 오류'));
            }
        })
        .catch(error => {
            console.error('설정 저장 실패:', error);
            hideLoading();
            alert('설정 저장 중 오류가 발생했습니다: ' + error.message);
        });
}

/**
 * 현재 설정 수집
 */
function collectCurrentSettings() {
    const recommendationTime = parseInt(document.getElementById('recommendation-time').value);

    const platformSettingsArray = [];
    Object.keys(platformSettings).forEach(platform => {
        const setting = platformSettings[platform];
        if (setting.enabled) {
            const platformLower = platform.toLowerCase();

            // 체크박스에서 난이도 수집
            const difficultyCheckboxes = document.querySelectorAll(`#${platformLower}-difficulty-container input[type="checkbox"]:checked`);
            const selectedDifficulties = Array.from(difficultyCheckboxes).map(cb => cb.value);

            // 체크박스에서 태그 수집
            const tagCheckboxes = document.querySelectorAll(`#${platformLower}-tags-container input[type="checkbox"]:checked`);
            const selectedTags = Array.from(tagCheckboxes).map(cb => cb.value);

            const countInput = document.getElementById(`${platformLower}-count`);
            const problemCount = parseInt(countInput.value);

            platformSettingsArray.push({
                platform: platform,
                difficulties: selectedDifficulties,
                tags: selectedTags,
                problemCount: problemCount,
                enabled: true
            });
        }
    });

    return {
        teamId: currentTeamId,
        platformSettings: platformSettingsArray,
        autoRecommendationEnabled: autoRecommendationEnabled,
        recommendationAt: recommendationTime,
        recommendDays: selectedDays
    };
}

/**
 * 설정 유효성 검사
 */
function validateSettings(settings) {
    // 활성화된 플랫폼이 있는지 확인
    if (settings.platformSettings.length === 0) {
        alert('최소 하나의 플랫폼을 활성화해야 합니다.');
        return false;
    }

    // 자동 추천이 활성화된 경우 요일 확인
    if (settings.autoRecommendationEnabled && settings.recommendDays.length === 0) {
        alert('자동 추천이 활성화된 경우 최소 하나의 요일을 선택해야 합니다.');
        return false;
    }

    // 플랫폼별 문제 수 확인
    for (const platformSetting of settings.platformSettings) {
        if (platformSetting.problemCount < 1 || platformSetting.problemCount > 10) {
            alert(`${platformSetting.platform} 플랫폼의 문제 수는 1-10개 사이여야 합니다.`);
            return false;
        }
    }

    return true;
}

/**
 * 현재 화면의 설정 초기화
 */
function resetCurrentSettings() {
    if (!currentTeamId) {
        alert('팀을 먼저 선택해주세요.');
        return;
    }

    if (!confirm('정말로 이 팀의 추천 설정을 초기화하시겠습니까?')) {
        return;
    }

    showLoading('설정을 초기화하는 중...');

    fetch(`/admin/recommendations/settings/${currentTeamId}`, {
        method: 'PUT'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            hideLoading();
            if (data.status === 'success') {
                alert('설정이 초기화되었습니다.');
                resetUISettings(); // UI 초기화
            } else {
                alert('설정 초기화에 실패했습니다: ' + (data.message || '알 수 없는 오류'));
            }
        })
        .catch(error => {
            console.error('설정 초기화 실패:', error);
            hideLoading();
            alert('설정 초기화 중 오류가 발생했습니다: ' + error.message);
        });
}

/**
 * UI 설정 초기화
 */
function resetUISettings() {
    // 플랫폼 버튼 초기화
    document.querySelectorAll('.platform-toggle-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    // 플랫폼 카드 초기화
    document.querySelectorAll('.platform-card').forEach(card => {
        card.classList.remove('enabled');
    });

    // 난이도 체크박스 초기화
    document.querySelectorAll('[id$="-difficulty-container"] input[type="checkbox"]').forEach(checkbox => {
        checkbox.checked = false;
        checkbox.closest('.form-check').classList.remove('selected');
    });

    // 태그 체크박스 초기화
    document.querySelectorAll('[id$="-tags-container"] input[type="checkbox"]').forEach(checkbox => {
        checkbox.checked = false;
        checkbox.closest('.form-check').classList.remove('selected');
    });

    // 문제 수 입력 필드 초기화
    ['baekjoon-count', 'programmers-count', 'leetcode-count'].forEach(inputId => {
        const input = document.getElementById(inputId);
        if (input) input.value = 2;
    });

    // 자동 추천 비활성화
    const autoToggle = document.getElementById('auto-recommendation-enabled');
    autoToggle.classList.remove('active');
    autoRecommendationEnabled = false;
    toggleScheduleSettings();

    // 요일 선택 초기화
    document.querySelectorAll('.day-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    selectedDays = [];

    // 추천 시간 초기화
    document.getElementById('recommendation-time').value = 9;

    // 선택된 날짜 초기화
    selectedDates = [];
    updateSelectedDatesDisplay();

    // 플랫폼 설정 초기화
    Object.keys(platformSettings).forEach(platform => {
        platformSettings[platform] = { enabled: false, difficulties: [], tags: [], problemCount: 2 };
    });

    // 플랫폼 상세 설정 숨김
    document.getElementById('platform-details').style.display = 'none';
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

console.log('문제 추천 설정 JavaScript 파일이 성공적으로 로드되었습니다!');
