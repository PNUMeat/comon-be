package site.codemonster.comon.domain.recommendation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import site.codemonster.comon.domain.problem.entity.ProblemStep;
import site.codemonster.comon.domain.problem.enums.Platform;

public record PlatformRecommendationRequest(
        @NotNull(message = "플랫폼은 필수입니다.")
        Platform platform,

        @NotNull(message = "ProblemStep은 필수입니다.")
        ProblemStep problemStep,

        @Min(1)
        int problemCount
) {}
