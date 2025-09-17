package site.codemonster.comon.domain.problem.collector;

import org.springframework.stereotype.Component;
import site.codemonster.comon.domain.problem.dto.request.ProblemInfoRequest;
import site.codemonster.comon.domain.problem.dto.response.ProblemInfoResponse;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.global.error.problem.ProblemValidationException;

import java.util.Set;

import static site.codemonster.comon.global.error.ErrorCode.*;

@Component
public class ProgrammersCollector implements ProblemCollector {

    private static final Set<String> VALID_PROGRAMMERS_LEVELS = Set.of(
            "Level 0", "Level 1", "Level 2", "Level 3", "Level 4", "Level 5"
    );

    private static final int MIN_PROBLEM_ID = 10000;
    private static final int MAX_PROBLEM_ID = 99999;
    private static final int MAX_TITLE_LENGTH = 50;

    @Override
    public ProblemInfoResponse collectProblemInfo(ProblemInfoRequest request) {
        validateRequest(request);

        return ProblemInfoResponse.builder()
                .platform(Platform.PROGRAMMERS)
                .platformProblemId(request.getPlatformProblemId())
                .title(request.getTitle().trim())
                .problemStep(request.getProblemStep())
                .url(buildProgrammersUrl(request.getPlatformProblemId()))
                .isDuplicate(false)
                .success(true)
                .build();
    }

    @Override
    public boolean isValidProblem(String problemId) {
        if (problemId == null || problemId.trim().isEmpty()) {
            return false;
        }

        try {
            int id = Integer.parseInt(problemId.trim());
            return id >= MIN_PROBLEM_ID && id <= MAX_PROBLEM_ID;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void validateRequest(ProblemInfoRequest request) {
        // 제목 검증
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new ProblemValidationException(PROBLEM_TITLE_REQUIRED_ERROR);
        }

        // 제목 길이 검증
        if (request.getTitle().length() > MAX_TITLE_LENGTH) {
            throw new ProblemValidationException(PROBLEM_TITLE_TOO_LONG_ERROR);
        }
    }

    private String buildProgrammersUrl(String problemId) {
        return "https://school.programmers.co.kr/learn/courses/30/lessons/" + problemId;
    }
}
