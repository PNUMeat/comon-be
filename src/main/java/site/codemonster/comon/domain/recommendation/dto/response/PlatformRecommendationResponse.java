package site.codemonster.comon.domain.recommendation.dto.response;

import site.codemonster.comon.domain.problem.entity.ProblemStep;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.recommendation.entity.PlatformRecommendation;

public record PlatformRecommendationResponse(
        Platform platform,
        ProblemStep problemStep,
        Integer problemCount
) {

    public PlatformRecommendationResponse(PlatformRecommendation platformRecommendation) {
        this(platformRecommendation.getPlatform(),  platformRecommendation.getProblemStep(), platformRecommendation.getProblemCount());
    }
}
