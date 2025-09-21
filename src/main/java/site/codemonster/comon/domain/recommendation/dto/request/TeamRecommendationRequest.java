package site.codemonster.comon.domain.recommendation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import site.codemonster.comon.domain.recommendation.annotation.DuplicateRecommendation;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

public record TeamRecommendationRequest(
        @NotNull(message = "팀 ID는 필수입니다.")
        @Positive(message = "팀 ID값은 양수여야 합니다.")
        Long teamId,

        @NotNull(message = "플랫폼 설정은 필수입니다.")
        @Size(min = 1, message = "추천할 플랫폼과 ProblemStep을 선택해야합니다.")
        @DuplicateRecommendation
        List<PlatformRecommendationRequest> platformRecommendationRequests,

        @NotNull(message = "추천 시간은 필수입니다.")
        Integer recommendationAt,

        @NotNull(message = "추천 요일 설정은 필수입니다.")
        Set<DayOfWeek> recommendDays
) {
}
