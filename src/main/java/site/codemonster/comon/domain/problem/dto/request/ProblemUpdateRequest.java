package site.codemonster.comon.domain.problem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import site.codemonster.comon.domain.problem.entity.ProblemStep;

public record ProblemUpdateRequest(
        @NotBlank
        String title,
        @NotNull
        ProblemStep problemStep,
        @NotBlank
        String url
) {
}
