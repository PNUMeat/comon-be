package site.codemonster.comon.domain.recommendation.service;

import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.entity.ProblemStep;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.recommendation.entity.RecommendationHistory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 플랫폼/STEP의 미추천 문제가 소진됐을 때, 팀의 추천 기록 중 가장 오래전에 추천한 문제부터 다시 고른다.
 * <p>
 * "오래전"의 기준은 기록이 저장된 순서(createdDate)다. recommendedAt(추천 대상 날짜)이 아니라 저장 순서를 쓰는 이유는,
 * 관리자가 과거 날짜로 수동 추천(백필)해도 방금 순환된 문제가 큐의 맨 뒤로 가야 같은 문제가 연달아 뽑히지 않기 때문이다.
 * 같은 문제가 여러 번 추천됐다면 가장 최근 기록을 기준으로 삼는다.
 */
public final class RecommendationProblemRecycler {

    private static final Comparator<RecommendationHistory> RECORDED_ORDER =
            Comparator.comparing(RecommendationHistory::getCreatedDate,
                    Comparator.nullsFirst(Comparator.<LocalDateTime>naturalOrder()));

    private RecommendationProblemRecycler() {}

    public static List<Problem> pickOldest(List<RecommendationHistory> histories,
                                           Platform platform,
                                           ProblemStep problemStep,
                                           int count) {
        if (count <= 0 || histories.isEmpty()) return List.of();

        Map<Long, RecommendationHistory> latestByProblem = new HashMap<>();
        for (RecommendationHistory history : histories) {
            Problem problem = history.getProblem();
            if (problem.getPlatform() != platform || problem.getProblemStep() != problemStep) continue;

            latestByProblem.merge(problem.getProblemId(), history,
                    (current, candidate) -> RECORDED_ORDER.compare(candidate, current) > 0 ? candidate : current);
        }

        return latestByProblem.values().stream()
                .sorted(RECORDED_ORDER.thenComparing(history -> history.getProblem().getProblemId()))
                .limit(count)
                .map(RecommendationHistory::getProblem)
                .toList();
    }
}
