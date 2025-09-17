package site.codemonster.comon.domain.recommendation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import site.codemonster.comon.domain.problem.entity.ProblemStep;
import site.codemonster.comon.domain.problem.enums.Platform;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

public record TeamRecommendationRequest(
        @NotNull(message = "팀 ID는 필수입니다.")
        @Positive(message = "팀 ID값은 양수여야 합니다.")
        Long teamId,

        @NotNull(message = "플랫폼 설정은 필수입니다.")
        @Size(min = 1, max = 3, message = "최소 하나의 플랫폼 설정이 필요합니다.")
        @Valid
        List<PlatformRecommendationSetting> platformSettings,

        @NotNull(message = "자동 추천 설정은 필수입니다.")
        Boolean autoRecommendationEnabled,

        @NotNull(message = "추천 시간은 필수입니다.")
        Integer recommendationAt,

        @NotNull(message = "추천 요일 설정은 필수입니다.")
        Set<DayOfWeek> recommendDays
) {
    public record PlatformRecommendationSetting(
            @NotNull(message = "플랫폼은 필수입니다.")
            Platform platform,

            @NotNull(message = "ProblemStep은 필수입니다.")
            ProblemStep problemStep,

            @NotNull(message = "문제 개수는 필수입니다.")
            @Min(value = 1, message = "문제 개수는 1개 이상이어야 합니다.")
            Integer problemCount,

            @NotNull(message = "활성화 여부는 필수입니다.")
            Boolean enabled
    ) {}
}
