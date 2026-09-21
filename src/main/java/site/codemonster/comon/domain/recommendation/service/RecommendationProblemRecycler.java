package site.codemonster.comon.domain.recommendation.service;

import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.entity.ProblemStep;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.recommendation.entity.RecommendationHistory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 플랫폼/STEP의 미추천 문제가 소진됐을 때, 팀의 추천 기록 중 가장 오래전에 추천한 문제부터 다시 고른다.
 * 같은 문제가 여러 번 추천됐다면 가장 최근 추천일을 기준으로 삼는다.
 */
public final class RecommendationProblemRecycler {

    private RecommendationProblemRecycler() {}

    public static List<Problem> pickOldest(List<RecommendationHistory> histories,
                                           Platform platform,
                                           ProblemStep problemStep,
                                           Set<Long> excludedProblemIds,
                                           int count) {
        if (count <= 0 || histories.isEmpty()) return List.of();

        Map<Long, RecommendationHistory> latestByProblem = new HashMap<>();
        for (RecommendationHistory history : histories) {
            Problem problem = history.getProblem();
            if (problem.getPlatform() != platform || problem.getProblemStep() != problemStep) continue;
            if (excludedProblemIds.contains(problem.getProblemId())) continue;

            latestByProblem.merge(problem.getProblemId(), history,
                    (current, candidate) -> candidate.getRecommendedAt().isAfter(current.getRecommendedAt()) ? candidate : current);
        }

        return latestByProblem.values().stream()
                .sorted(Comparator.comparing(RecommendationHistory::getRecommendedAt)
                        .thenComparing(history -> history.getProblem().getProblemId()))
                .limit(count)
                .map(RecommendationHistory::getProblem)
                .toList();
    }
}
