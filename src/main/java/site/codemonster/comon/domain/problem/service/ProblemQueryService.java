package site.codemonster.comon.domain.problem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.problem.repository.ProblemRepository;
import site.codemonster.comon.global.error.problem.ProblemNotFoundException;
import site.codemonster.comon.global.error.problem.ProblemValidationException;

import java.util.List;
import java.util.Map;

import static site.codemonster.comon.global.error.ErrorCode.PROBLEM_PLATFORM_REQUIRED_ERROR;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemQueryService {

    private final ProblemRepository problemRepository;

    public boolean checkDuplicateProblem(Platform platform, String problemId) {
        return problemRepository.existsByPlatformAndPlatformProblemId(platform, problemId);
    }

    public Map<Platform, Long> getProblemStatistics() {
        return Map.of(
                Platform.BAEKJOON, problemRepository.countByPlatform(Platform.BAEKJOON),
                Platform.LEETCODE, problemRepository.countByPlatform(Platform.LEETCODE),
                Platform.PROGRAMMERS, problemRepository.countByPlatform(Platform.PROGRAMMERS)
        );
    }

    public List<Problem> getAllProblems() {
        return problemRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public List<Problem> getProblemsByPlatform(Platform platform) {
        if (platform == null) {
            throw new ProblemValidationException(PROBLEM_PLATFORM_REQUIRED_ERROR);
        }
        return problemRepository.findByPlatform(platform);
    }

    public Problem findProblemById(Long problemId) {
        return problemRepository.findById(problemId)
                .orElseThrow(ProblemNotFoundException::new);
    }
}
