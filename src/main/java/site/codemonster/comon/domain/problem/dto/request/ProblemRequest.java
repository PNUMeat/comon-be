package site.codemonster.comon.domain.problem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import site.codemonster.comon.domain.problem.entity.ProblemStep;

public record ProblemRequest(
        @NotBlank
        String platformProblemId,
        @NotNull
        ProblemStep problemStep,
        String title
) {
}
