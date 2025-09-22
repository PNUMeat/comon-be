package site.codemonster.comon.domain.problem.dto.response;

import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.entity.ProblemStep;
import site.codemonster.comon.domain.problem.enums.Platform;

public record ProblemResponse(
        Long problemId,
        Platform platform,
        String platformProblemId,
        String title,
        ProblemStep problemStep,
        String url
) {
    public ProblemResponse(Problem problem) {
        this(
                problem.getProblemId(),  problem.getPlatform(),
                problem.getPlatformProblemId(), problem.getTitle(),
                problem.getProblemStep(), problem.getUrl()
        );
    }
}
