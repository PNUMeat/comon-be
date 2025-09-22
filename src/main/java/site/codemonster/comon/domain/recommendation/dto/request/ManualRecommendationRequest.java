package site.codemonster.comon.domain.recommendation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.Set;

public record ManualRecommendationRequest(
        @NotNull(message = "팀 ID는 필수입니다.")
        @Positive(message = "팀 ID는 양수여야 합니다.")
        Long teamId,

        @NotEmpty(message = "선택된 날짜는 필수입니다.")
        Set<LocalDate> selectedDates
) {}
