// problem-recommendation.js - 문제 추천 설정 페이지 JavaScript

let currentTeamId = null;
let selectedDays = [];       // ["MONDAY", ...]
let selectedDates = [];      // ["2025-01-01", ...] for manual

// ==================== 초기화 ====================
document.addEventListener("DOMContentLoaded", () => {
    console.log("문제 추천 설정 페이지 초기화");
    loadTeamOptions();
});

// ==================== 팀 불러오기 ====================
function loadTeamOptions() {
    fetch("/api/v1/teams/all")
        .then(res => res.json())
        .then(data => {
            if (data.status === "success") {
                populateTeamOptions(data.data);
            } else {
                console.error("팀 목록 로드 실패:", data.message);
            }
        })
        .catch(err => console.error("팀 목록 불러오기 오류:", err));
}

function populateTeamOptions(teams) {
    const teamSelect = document.getElementById("team-select");
    teamSelect.innerHTML = '<option value="">팀을 선택하세요</option>';

    (teams || []).forEach(team => {
        const opt = document.createElement("option");
        opt.value = team.teamId;
        opt.textContent = `${team.teamName} (${team.memberCount}명)`;
        teamSelect.appendChild(opt);
    });
}

// ==================== 팀 변경 ====================
function handleTeamChange() {
    const teamSelect = document.getElementById("team-select");
    currentTeamId = teamSelect.value;

    const show = !!currentTeamId;
    document.getElementById("platform-settings-card").style.display = show ? "block" : "none";
    document.getElementById("schedule-settings-card").style.display = show ? "block" : "none";
    document.getElementById("save-buttons").style.display = show ? "block" : "none";
    document.getElementById("manual-recommendation-card").style.display = show ? "block" : "none";

    document.getElementById("load-btn").disabled = !show;
    document.getElementById("delete-btn").disabled = !show;
    document.getElementById("manual-recommend-btn").disabled = !show;

    if (!show) {
        clearUiOnly();
    }
}

// ==================== 설정 목록 UI 조작 ====================
function addPlatformRecommendation() {
    const platform = document.getElementById("platform-select").value;
    const step = document.getElementById("step-select").value;
    const count = parseInt(document.getElementById("problem-count").value);

    if (!platform || !step || !count) {
        alert("플랫폼, 난이도, 문제 수를 모두 선택하세요.");
        return;
    }

    const tbody = document.getElementById("recommendation-list");

    // 중복 체크 (플랫폼 + 난이도 동일 조합 방지)
    const rows = tbody.querySelectorAll("tr");
    for (let row of rows) {
        const cols = row.querySelectorAll("td");
        if (cols.length >= 3) {
            const existingPlatform = cols[0].innerText.trim();
            const existingStep = cols[1].innerText.trim();
            if (existingPlatform === platform && existingStep === step) {
                alert("이미 같은 플랫폼과 난이도의 설정이 존재합니다.");
                return;
            }
        }
    }

    // placeholder 제거
    if (tbody.querySelector("td.text-center")) {
        tbody.innerHTML = "";
    }

    // 새 행 추가
    const tr = document.createElement("tr");
    tr.innerHTML = `
        <td>${platform}</td>
        <td>${step}</td>
        <td>${count}</td>
        <td>
            <button type="button" class="btn btn-sm btn-outline-danger" onclick="this.closest('tr').remove(); ensurePlaceholderRow();">
                삭제
            </button>
        </td>
    `;
    tbody.appendChild(tr);
}

function ensurePlaceholderRow() {
    const tbody = document.getElementById("recommendation-list");
    if (!tbody.querySelector("tr")) {
        tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">아직 추가된 설정이 없습니다</td></tr>`;
    }
}

// 요일 토글
function toggleDay(btn) {
    const day = btn.getAttribute("data-day");
    if (btn.classList.contains("active")) {
        btn.classList.remove("active");
        selectedDays = selectedDays.filter(d => d !== day);
    } else {
        btn.classList.add("active");
        if (!selectedDays.includes(day)) selectedDays.push(day);
    }
}

// ==================== 설정 저장 / 조회 / 삭제 ====================

// 저장 (POST /admin/recommendations/settings)
function saveSettings() {
    if (!currentTeamId) {
        alert("팀을 먼저 선택하세요.");
        return;
    }

    const hour = parseInt(document.getElementById("recommendation-time").value);
    const platformSettings = [];

    document.querySelectorAll("#recommendation-list tr").forEach(row => {
        const cols = row.querySelectorAll("td");
        if (cols.length === 4 && !cols[0].classList.contains("text-center")) {
            platformSettings.push({
                platform: cols[0].innerText.trim(),
                problemStep: cols[1].innerText.trim(),
                problemCount: parseInt(cols[2].innerText.trim())
            });
        }
    });

    if (platformSettings.length === 0) {
        alert("최소 하나의 추천 설정을 추가해야 합니다.");
        return;
    }

    const payload = {
        teamId: parseInt(currentTeamId),
        platformRecommendationRequests: platformSettings,
        recommendationAt: hour,
        recommendDays: selectedDays // ["MONDAY", ...]
    };

    fetch("/admin/recommendations/settings", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
        .then(res => res.json())
        .then(data => {
            if (data.status === "success") {
                alert(data.message || "설정이 저장되었습니다.");
            } else {
                alert("저장 실패: " + (data.message || "알 수 없는 오류"));
            }
        })
        .catch(err => {
            console.error("설정 저장 실패:", err);
            alert("저장 중 오류 발생: " + err.message);
        });
}

// 조회 (GET /admin/recommendations/settings/{teamId})
function loadTeamSettings() {
    if (!currentTeamId) return;

    fetch(`/admin/recommendations/settings/${currentTeamId}`)
        .then(res => res.json())
        .then(data => {
            if (data.status !== "success") {
                alert(data.message || "설정 조회에 실패했습니다.");
                return;
            }
            const res = data.data; // TeamRecommendationResponse
            applyTeamSettingsToUI(res);
            alert(data.message || "설정을 불러왔습니다.");
        })
        .catch(err => {
            console.error("설정 조회 실패:", err);
            alert("설정 조회 중 오류 발생: " + err.message);
        });
}

function applyTeamSettingsToUI(res) {
    // 시간
    const timeSel = document.getElementById("recommendation-time");
    if (typeof res.recommendationAt === "number") {
        timeSel.value = String(res.recommendationAt);
    }

    // 요일
    selectedDays = Array.isArray(res.recommendDays) ? res.recommendDays.slice() : [];
    document.querySelectorAll(".day-btn").forEach(btn => {
        const day = btn.getAttribute("data-day");
        if (selectedDays.includes(day)) btn.classList.add("active");
        else btn.classList.remove("active");
    });

    // 플랫폼 추천 목록
    const tbody = document.getElementById("recommendation-list");
    tbody.innerHTML = "";

    const list = res.platformRecommendationResponses || [];
    if (list.length === 0) {
        ensurePlaceholderRow();
    } else {
        list.forEach(item => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${item.platform}</td>
                <td>${item.problemStep}</td>
                <td>${item.problemCount}</td>
                <td>
                    <button type="button" class="btn btn-sm btn-outline-danger" onclick="this.closest('tr').remove(); ensurePlaceholderRow();">
                        삭제
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }
}

// 삭제(초기화) (DELETE /admin/recommendations/settings/{teamId})
function deleteTeamSettings() {
    if (!currentTeamId) return;

    if (!confirm("정말로 이 팀의 추천 설정을 초기화하시겠습니까?")) return;

    fetch(`/admin/recommendations/settings/${currentTeamId}`, { method: "DELETE" })
        .then(res => res.json())
        .then(data => {
            if (data.status === "success") {
                alert(data.message || "설정이 초기화되었습니다.");
                clearUiOnly();
            } else {
                alert("초기화 실패: " + (data.message || "알 수 없는 오류"));
            }
        })
        .catch(err => {
            console.error("설정 삭제 실패:", err);
            alert("초기화 중 오류 발생: " + err.message);
        });
}

// 화면만 초기화(서버 값은 유지)
function clearUiOnly() {
    // 목록 테이블
    document.getElementById("recommendation-list").innerHTML =
        `<tr><td colspan="4" class="text-center text-muted">아직 추가된 설정이 없습니다</td></tr>`;

    // 시간/요일
    document.getElementById("recommendation-time").value = "9";
    selectedDays = [];
    document.querySelectorAll(".day-btn").forEach(btn => btn.classList.remove("active"));

    // 입력 필드
    document.getElementById("platform-select").value = "";
    document.getElementById("step-select").value = "";
    document.getElementById("problem-count").value = "2";

    // 수동 추천 선택 날짜
    selectedDates = [];
    refreshSelectedDatesUi();
}

// ==================== 수동 추천 (POST /admin/recommendations/manual) ====================
function addSelectedDate() {
    const input = document.getElementById("manual-date-picker");
    const val = input.value; // "YYYY-MM-DD"
    if (!val) return;
    if (!selectedDates.includes(val)) selectedDates.push(val);
    input.value = "";
    refreshSelectedDatesUi();
}

function removeSelectedDate(dateStr) {
    selectedDates = selectedDates.filter(d => d !== dateStr);
    refreshSelectedDatesUi();
}

function refreshSelectedDatesUi() {
    const container = document.getElementById("selected-dates");
    const list = document.getElementById("date-list");
    list.innerHTML = "";

    if (selectedDates.length === 0) {
        container.style.display = "none";
        document.getElementById("manual-recommend-btn").disabled = true;
        return;
    }

    container.style.display = "block";
    document.getElementById("manual-recommend-btn").disabled = !currentTeamId;

    selectedDates.forEach(d => {
        const tag = document.createElement("span");
        tag.className = "date-tag";
        tag.innerHTML = `${d}<button class="remove-date" onclick="removeSelectedDate('${d}')">&times;</button>`;
        list.appendChild(tag);
    });
}

function executeManualRecommendation() {
    if (!currentTeamId) {
        alert("팀을 먼저 선택하세요.");
        return;
    }
    if (selectedDates.length === 0) {
        alert("추천할 날짜를 선택하세요.");
        return;
    }

    const payload = {
        teamId: parseInt(currentTeamId),
        selectedDates: selectedDates // ["2025-01-01", ...]
    };

    fetch("/admin/recommendations/manual", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
        .then(res => res.json())
        .then(data => {
            if (data.status === "success") {
                // ManualRecommendationResponse: totalRecommended, processedDates, createdArticleTitles, message
                const msg = data.data?.message || data.message || "수동 추천이 완료되었습니다.";
                alert(msg);
            } else {
                alert("수동 추천 실패: " + (data.message || "알 수 없는 오류"));
            }
        })
        .catch(err => {
            console.error("수동 추천 실패:", err);
            alert("수동 추천 중 오류 발생: " + err.message);
        });
}
