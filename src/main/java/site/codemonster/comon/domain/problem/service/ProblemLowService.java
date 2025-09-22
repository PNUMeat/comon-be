package site.codemonster.comon.domain.problem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.problem.dto.response.ProblemResponse;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.problem.repository.ProblemRepository;
import site.codemonster.comon.domain.recommendation.entity.PlatformRecommendation;
import site.codemonster.comon.global.error.problem.ProblemNotFoundException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ProblemLowService {

    private final ProblemRepository problemRepository;

    public Problem save(Problem problem) {
        return problemRepository.save(problem);
    }


    public void delete(Problem problem) {
        problemRepository.delete(problem);
    }

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

    public List<ProblemResponse> getAllProblems() {
        return problemRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"))
                .stream().map(ProblemResponse::new).toList();
    }

    public List<ProblemResponse> getProblemsByPlatform(Platform platform) {
        return problemRepository.findByPlatform(platform)
                .stream().map(ProblemResponse::new).toList();
    }

    public Problem findProblemById(Long problemId) {
        return problemRepository.findById(problemId)
                .orElseThrow(ProblemNotFoundException::new);
    }

    public List<Problem> findRecommendationProblem(List<Long> excludedIds, PlatformRecommendation platformRecommendation) {


        return problemRepository.findRecommendationProblem(excludedIds, platformRecommendation.getPlatform(),
                platformRecommendation.getProblemStep(),
                PageRequest.of(0, platformRecommendation.getProblemCount()))
                .stream().toList();
    }
}
