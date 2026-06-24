package site.codemonster.comon.domain.recommendation.dto.response;

import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.entity.ProblemStep;
import site.codemonster.comon.domain.problem.enums.Platform;

public record RecommendationProblemResponse(
        ProblemStep step,
        Platform platform,
        String title,
        String url
) {
    public static RecommendationProblemResponse from(Problem problem) {
        return new RecommendationProblemResponse(
                problem.getProblemStep(),
                problem.getPlatform(),
                problem.getTitle(),
                problem.getUrl()
        );
    }
}
