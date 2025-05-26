package site.codemonster.comon.domain.article.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CalenderSubjectRequest (
        @NotNull(message = "년도는 필수입니다.")
        @Positive(message = "년도는 양의 정수 입니다.")
        Integer year,
        @NotNull(message = "달은 필수입니다.")
        @Positive(message = "달은 양의 정수 입니다.")
        Integer month
) {
}
