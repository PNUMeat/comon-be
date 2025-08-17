package site.codemonster.comon.domain.problem.collector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.codemonster.comon.domain.problem.dto.request.ProblemInfoRequest;
import site.codemonster.comon.domain.problem.dto.response.ProblemInfoResponse;
import site.codemonster.comon.domain.problem.enums.Platform;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ProgrammersCollector implements ProblemCollector {

    private static final List<String> PROGRAMMERS_LEVELS = Arrays.asList(
            "Level 0", "Level 1", "Level 2", "Level 3", "Level 4", "Level 5"
    );

    @Override
    public ProblemInfoResponse collectProblemInfo(ProblemInfoRequest request) {
        validateRequest(request);

        try {
            String url = "https://school.programmers.co.kr/learn/courses/30/lessons/" + request.getPlatformProblemId();

            return ProblemInfoResponse.builder()
                    .platform(Platform.PROGRAMMERS)
                    .platformProblemId(request.getPlatformProblemId())
                    .title(request.getTitle())
                    .difficulty(request.getDifficulty())
                    .url(url)
                    .tags(request.getTags() != null ? request.getTags() : "")
                    .isDuplicate(false)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("프로그래머스 문제 정보 처리 실패: {}", e.getMessage());
            throw new RuntimeException("프로그래머스 문제 정보 처리에 실패했습니다", e);
        }
    }

    @Override
    public boolean isValidProblem(String problemId) {
        if (problemId == null || problemId.trim().isEmpty()) {
            return false;
        }

        try {
            int id = Integer.parseInt(problemId.trim());
            return id > 0 && id <= 999999;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void validateRequest(ProblemInfoRequest request) {
        if (request.getPlatformProblemId() == null || request.getPlatformProblemId().trim().isEmpty()) {
            throw new IllegalArgumentException("문제번호를 입력해주세요.");
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("문제 제목을 입력해주세요.");
        }

        if (request.getDifficulty() == null || request.getDifficulty().trim().isEmpty()) {
            throw new IllegalArgumentException("난이도를 선택해주세요.");
        }

        if (!PROGRAMMERS_LEVELS.contains(request.getDifficulty())) {
            throw new IllegalArgumentException("유효하지 않은 난이도입니다: " + request.getDifficulty());
        }

        if (!isValidProblem(request.getPlatformProblemId())) {
            throw new IllegalArgumentException("유효하지 않은 문제번호입니다: " + request.getPlatformProblemId());
        }

        if (request.getTitle().length() > 100) {
            throw new IllegalArgumentException("제목이 너무 깁니다. (최대 100자)");
        }

        if (request.getTags() != null && request.getTags().length() > 200) {
            throw new IllegalArgumentException("태그가 너무 깁니다. (최대 200자)");
        }
    }
}
