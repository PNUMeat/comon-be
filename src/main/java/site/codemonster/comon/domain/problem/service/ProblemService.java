package site.codemonster.comon.domain.problem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.problem.collector.LeetcodeCollector;
import site.codemonster.comon.domain.problem.collector.ProblemCollector;
import site.codemonster.comon.domain.problem.collector.ProblemCollectorFactory;
import site.codemonster.comon.domain.problem.dto.request.ProblemInfoRequest;
import site.codemonster.comon.domain.problem.dto.response.ProblemInfoResponse;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.problem.repository.ProblemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final ProblemCollectorFactory collectorFactory;
    private final LeetcodeCollector leetcodeCollector;

    public ProblemInfoResponse checkProblem(String problemInput, Platform platform) {
        String problemId = extractProblemId(problemInput, platform);

        if (checkDuplicateProblem(platform, problemId)) {
            return createDuplicateResponse(platform, problemId, problemInput);
        }

        return collectProblemInfo(problemInput, platform);
    }

    public ProblemInfoResponse checkProblem(ProblemInfoRequest request) {
        Platform platform = request.getPlatform();
        String problemId = request.getPlatformProblemId();

        if (checkDuplicateProblem(platform, problemId)) {
            return createDuplicateResponse(platform, problemId, null);
        }

        return collectProblemInfoFromRequest(request);
    }

    private String extractProblemId(String problemInput, Platform platform) {
        if (platform == Platform.LEETCODE) {
            return leetcodeCollector.extractSlugFromUrl(problemInput, true);
        }
        return problemInput;
    }

    private ProblemInfoResponse collectProblemInfo(String problemInput, Platform platform) {
        try {
            ProblemCollector collector = collectorFactory.getCollector(platform);

            if (!collector.isValidProblem(problemInput)) {
                throw new IllegalArgumentException("유효하지 않은 문제입니다: " + problemInput);
            }

            ProblemInfoRequest collectorRequest = ProblemInfoRequest.builder()
                    .platform(platform)
                    .platformProblemId(problemInput)
                    .build();

            return collector.collectProblemInfo(collectorRequest);
        } catch (Exception e) {
            throw new RuntimeException(platform.getName() + " 문제 정보를 수집할 수 없습니다: " + e.getMessage(), e);
        }
    }

    private ProblemInfoResponse collectProblemInfoFromRequest(ProblemInfoRequest request) {
        try {
            ProblemCollector collector = collectorFactory.getCollector(request.getPlatform());

            if (!collector.isValidProblem(request.getPlatformProblemId())) {
                throw new IllegalArgumentException("유효하지 않은 문제번호입니다: " + request.getPlatformProblemId());
            }

            return collector.collectProblemInfo(request);
        } catch (Exception e) {
            throw new RuntimeException(request.getPlatform().getName() + " 문제 정보를 수집할 수 없습니다: " + e.getMessage(), e);
        }
    }

    private ProblemInfoResponse createDuplicateResponse(Platform platform, String problemId, String originalInput) {
        String url = switch (platform) {
            case BAEKJOON -> "https://www.acmicpc.net/problem/" + problemId;
            case PROGRAMMERS -> "https://school.programmers.co.kr/learn/courses/30/lessons/" + problemId;
            case LEETCODE -> originalInput;
        };

        return ProblemInfoResponse.builder()
                .platform(platform)
                .platformProblemId(problemId)
                .title("(중복된 문제)")
                .difficulty("")
                .url(url)
                .tags("")
                .isDuplicate(true)
                .success(true)
                .build();
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

    @Transactional
    public List<Problem> registerProblems(List<ProblemInfoRequest> requests) throws InterruptedException {
        List<Problem> savedProblems = new ArrayList<>();

        for (ProblemInfoRequest request : requests) {
            if (checkDuplicateProblem(request.getPlatform(), request.getPlatformProblemId())) {
                continue;
            }

            Problem savedProblem = saveProblem(request);
            savedProblems.add(savedProblem);
            Thread.sleep(100);
        }
        return savedProblems;
    }

    private Problem saveProblem(ProblemInfoRequest request) {
        Problem problem = Problem.builder()
                .platform(request.getPlatform())
                .platformProblemId(request.getPlatformProblemId())
                .title(request.getTitle())
                .difficulty(request.getDifficulty())
                .url(request.getUrl())
                .tags(request.getTags())
                .build();

        return problemRepository.save(problem);
    }

    public List<Problem> getAllProblems() {
        return problemRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    @Transactional
    public Problem updateProblem(Long problemId, Map<String, String> updateData) {
        Problem problem = findProblemById(problemId);
        updateProblemFields(problem, updateData);

        return problemRepository.save(problem);
    }

    @Transactional
    public void deleteProblem(Long problemId) {
        Problem problem = findProblemById(problemId);
        problemRepository.delete(problem);
    }

    private Problem findProblemById(Long problemId) {
        return problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("문제를 찾을 수 없습니다: " + problemId));
    }

    private void updateProblemFields(Problem problem, Map<String, String> updateData) {
        updateData.forEach((key, value) -> {
            switch (key) {
                case "title" -> problem.setTitle(value);
                case "difficulty" -> problem.setDifficulty(value);
                case "tags" -> problem.setTags(value);
                case "url" -> problem.setUrl(value);
            }
        });
    }

    public List<Problem> getProblemsByPlatform(Platform platform) {
        return problemRepository.findByPlatform(platform);
    }
}
