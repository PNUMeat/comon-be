package site.codemonster.comon.domain.recommendation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.entity.ProblemStep;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.recommendation.entity.RecommendationHistory;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.util.TestUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationProblemRecyclerTest {

    private final Team team = TestUtil.createTeamWithId();

    private final Problem step1Jan = problem(1L, Platform.PROGRAMMERS, ProblemStep.STEP1);
    private final Problem step1Feb = problem(2L, Platform.PROGRAMMERS, ProblemStep.STEP1);
    private final Problem step1Mar = problem(3L, Platform.PROGRAMMERS, ProblemStep.STEP1);
    private final Problem step2Problem = problem(4L, Platform.PROGRAMMERS, ProblemStep.STEP2);
    private final Problem baekjoonStep1 = problem(5L, Platform.BAEKJOON, ProblemStep.STEP1);

    // 기록 저장 순서: step1Jan(2025-11) -> step2Problem, baekjoonStep1(2025-12) -> step1Jan(2026-01) -> step1Feb(2026-02) -> step1Mar(2026-03)
    private final List<RecommendationHistory> histories = List.of(
            history(step1Mar, LocalDate.of(2026, 3, 1)),
            history(step1Jan, LocalDate.of(2026, 1, 1)),
            history(step1Feb, LocalDate.of(2026, 2, 1)),
            history(step1Jan, LocalDate.of(2025, 11, 1)), // 같은 문제의 더 오래된 기록 (최신 기록 기준으로 판단)
            history(step2Problem, LocalDate.of(2025, 12, 1)),
            history(baekjoonStep1, LocalDate.of(2025, 12, 1))
    );

    @Test
    @DisplayName("같은 플랫폼/STEP 문제를 마지막 기록이 오래된 순으로 고른다")
    void picksOldestFirstWithinPlatformAndStep() {
        List<Problem> picked = RecommendationProblemRecycler.pickOldest(
                histories, Platform.PROGRAMMERS, ProblemStep.STEP1, 2);

        assertThat(picked).containsExactly(step1Jan, step1Feb);
    }

    @Test
    @DisplayName("같은 문제가 여러 번 추천됐으면 가장 최근 기록을 기준으로 정렬한다")
    void usesLatestRecordPerProblem() {
        List<RecommendationHistory> twice = List.of(
                history(step1Feb, LocalDate.of(2026, 2, 1)),
                history(step1Jan, LocalDate.of(2025, 1, 1)),
                history(step1Jan, LocalDate.of(2026, 3, 1)) // 1월 문제가 3월에 다시 추천됨
        );

        List<Problem> picked = RecommendationProblemRecycler.pickOldest(
                twice, Platform.PROGRAMMERS, ProblemStep.STEP1, 1);

        assertThat(picked).containsExactly(step1Feb);
    }

    @Test
    @DisplayName("과거 날짜로 백필해도 방금 순환된 문제는 큐의 맨 뒤로 간다 (저장 순서 기준)")
    void recycledProblemMovesToBackEvenWhenBackfillingPastDates() {
        List<RecommendationHistory> withBackfill = new ArrayList<>(histories);
        // 2026-01-05로 백필 실행: step1Jan이 순환됨. recommendedAt은 과거지만 기록 자체는 지금 저장됨
        withBackfill.add(history(step1Jan, LocalDate.of(2026, 1, 5), LocalDate.of(2026, 9, 21)));

        List<Problem> picked = RecommendationProblemRecycler.pickOldest(
                withBackfill, Platform.PROGRAMMERS, ProblemStep.STEP1, 3);

        assertThat(picked).containsExactly(step1Feb, step1Mar, step1Jan);
    }

    @Test
    @DisplayName("요청 수보다 후보가 적으면 있는 만큼만 돌려준다")
    void returnsFewerWhenNotEnoughCandidates() {
        List<Problem> picked = RecommendationProblemRecycler.pickOldest(
                histories, Platform.PROGRAMMERS, ProblemStep.STEP1, 10);

        assertThat(picked).containsExactly(step1Jan, step1Feb, step1Mar);
    }

    @Test
    @DisplayName("요청 수가 0이거나 기록이 없으면 빈 목록")
    void returnsEmptyForZeroCountOrNoHistory() {
        assertThat(RecommendationProblemRecycler.pickOldest(
                histories, Platform.PROGRAMMERS, ProblemStep.STEP1, 0)).isEmpty();
        assertThat(RecommendationProblemRecycler.pickOldest(
                List.of(), Platform.PROGRAMMERS, ProblemStep.STEP1, 3)).isEmpty();
    }

    @Test
    @DisplayName("저장 시각이 없는 옛 기록은 가장 오래된 것으로 취급한다")
    void treatsMissingCreatedDateAsOldest() {
        RecommendationHistory legacy = new RecommendationHistory(team, step1Mar, LocalDate.of(2026, 3, 1)); // createdDate 없음
        List<RecommendationHistory> mixed = List.of(
                history(step1Jan, LocalDate.of(2026, 1, 1)),
                legacy
        );

        List<Problem> picked = RecommendationProblemRecycler.pickOldest(
                mixed, Platform.PROGRAMMERS, ProblemStep.STEP1, 1);

        assertThat(picked).containsExactly(step1Mar);
    }

    private static Problem problem(Long id, Platform platform, ProblemStep step) {
        Problem problem = new Problem(platform, String.valueOf(id), "문제" + id, step, "url");
        ReflectionTestUtils.setField(problem, "problemId", id);
        return problem;
    }

    /** 평소 흐름: 추천 당일에 기록이 저장되므로 createdDate = recommendedAt */
    private RecommendationHistory history(Problem problem, LocalDate recommendedAt) {
        return history(problem, recommendedAt, recommendedAt);
    }

    private RecommendationHistory history(Problem problem, LocalDate recommendedAt, LocalDate createdOn) {
        RecommendationHistory history = new RecommendationHistory(team, problem, recommendedAt);
        ReflectionTestUtils.setField(history, "createdDate", createdOn.atStartOfDay());
        return history;
    }
}
