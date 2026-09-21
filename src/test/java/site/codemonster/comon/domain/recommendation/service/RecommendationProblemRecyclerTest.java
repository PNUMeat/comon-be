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
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationProblemRecyclerTest {

    private final Team team = TestUtil.createTeamWithId();

    private final Problem step1Jan = problem(1L, Platform.PROGRAMMERS, ProblemStep.STEP1);
    private final Problem step1Feb = problem(2L, Platform.PROGRAMMERS, ProblemStep.STEP1);
    private final Problem step1Mar = problem(3L, Platform.PROGRAMMERS, ProblemStep.STEP1);
    private final Problem step2Problem = problem(4L, Platform.PROGRAMMERS, ProblemStep.STEP2);
    private final Problem baekjoonStep1 = problem(5L, Platform.BAEKJOON, ProblemStep.STEP1);

    private final List<RecommendationHistory> histories = List.of(
            history(step1Mar, LocalDate.of(2026, 3, 1)),
            history(step1Jan, LocalDate.of(2026, 1, 1)),
            history(step1Feb, LocalDate.of(2026, 2, 1)),
            history(step1Jan, LocalDate.of(2025, 11, 1)), // 같은 문제의 더 오래된 기록 (최신 기록 기준으로 판단)
            history(step2Problem, LocalDate.of(2025, 1, 1)),
            history(baekjoonStep1, LocalDate.of(2025, 1, 1))
    );

    @Test
    @DisplayName("같은 플랫폼/STEP 문제를 마지막 추천일이 오래된 순으로 고른다")
    void picksOldestFirstWithinPlatformAndStep() {
        List<Problem> picked = RecommendationProblemRecycler.pickOldest(
                histories, Platform.PROGRAMMERS, ProblemStep.STEP1, Set.of(), 2);

        assertThat(picked).containsExactly(step1Jan, step1Feb);
    }

    @Test
    @DisplayName("같은 문제가 여러 번 추천됐으면 가장 최근 추천일을 기준으로 정렬한다")
    void usesLatestRecommendationDatePerProblem() {
        List<RecommendationHistory> twice = List.of(
                history(step1Feb, LocalDate.of(2026, 2, 1)),
                history(step1Jan, LocalDate.of(2025, 1, 1)),
                history(step1Jan, LocalDate.of(2026, 3, 1)) // 1월 문제가 3월에 다시 추천됨
        );

        List<Problem> picked = RecommendationProblemRecycler.pickOldest(
                twice, Platform.PROGRAMMERS, ProblemStep.STEP1, Set.of(), 1);

        assertThat(picked).containsExactly(step1Feb);
    }

    @Test
    @DisplayName("제외 목록에 있는 문제는 건너뛴다")
    void skipsExcludedProblems() {
        List<Problem> picked = RecommendationProblemRecycler.pickOldest(
                histories, Platform.PROGRAMMERS, ProblemStep.STEP1, Set.of(1L), 2);

        assertThat(picked).containsExactly(step1Feb, step1Mar);
    }

    @Test
    @DisplayName("요청 수보다 후보가 적으면 있는 만큼만 돌려준다")
    void returnsFewerWhenNotEnoughCandidates() {
        List<Problem> picked = RecommendationProblemRecycler.pickOldest(
                histories, Platform.PROGRAMMERS, ProblemStep.STEP1, Set.of(), 10);

        assertThat(picked).containsExactly(step1Jan, step1Feb, step1Mar);
    }

    @Test
    @DisplayName("요청 수가 0이거나 기록이 없으면 빈 목록")
    void returnsEmptyForZeroCountOrNoHistory() {
        assertThat(RecommendationProblemRecycler.pickOldest(
                histories, Platform.PROGRAMMERS, ProblemStep.STEP1, Set.of(), 0)).isEmpty();
        assertThat(RecommendationProblemRecycler.pickOldest(
                List.of(), Platform.PROGRAMMERS, ProblemStep.STEP1, Set.of(), 3)).isEmpty();
    }

    private static Problem problem(Long id, Platform platform, ProblemStep step) {
        Problem problem = new Problem(platform, String.valueOf(id), "문제" + id, step, "url");
        ReflectionTestUtils.setField(problem, "problemId", id);
        return problem;
    }

    private RecommendationHistory history(Problem problem, LocalDate recommendedAt) {
        return new RecommendationHistory(team, problem, recommendedAt);
    }
}
