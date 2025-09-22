package site.codemonster.comon.domain.recommendation.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record TeamRecommendationRequest(
        @NotNull(message = "팀 ID는 필수입니다.")
        @Positive(message = "팀 ID값은 양수여야 합니다.")
        Long teamId,

        @NotNull(message = "플랫폼 설정은 필수입니다.")
        @Size(min = 1, message = "추천할 플랫폼과 ProblemStep을 선택해야합니다.")
        List<PlatformRecommendationRequest> platformRecommendationRequests,

        @NotNull(message = "추천 시간은 필수입니다.")
        Integer recommendationAt,

        @NotNull(message = "추천 요일 설정은 필수입니다.")
        @Size(min = 1, max = 7, message = "추천 요일 설정은 필수입니다.")
        Set<DayOfWeek> recommendDays
) {

        @AssertTrue(message = "중복된 Platform + ProblemStep 조합입니다.")
        public boolean isNoDuplicateRecommendation() {
                Set<String> combinations = new HashSet<>();
                for (PlatformRecommendationRequest request : platformRecommendationRequests) {
                        String combination = request.platform().name() + "-" + request.problemStep().name();
                        if (!combinations.add(combination)) return false;
                }
                return true;
        }
}
