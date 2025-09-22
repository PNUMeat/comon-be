package site.codemonster.comon.domain.problem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.problem.collector.ProblemCollector;
import site.codemonster.comon.domain.problem.collector.ProblemCollectorFactory;
import site.codemonster.comon.domain.problem.dto.request.ProblemInfoRequest;
import site.codemonster.comon.domain.problem.dto.request.ProblemRequest;
import site.codemonster.comon.domain.problem.dto.request.ProblemUpdateRequest;
import site.codemonster.comon.domain.problem.dto.response.ProblemInfoResponse;
import site.codemonster.comon.domain.problem.dto.response.ProblemResponse;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.problem.repository.ProblemRepository;
import site.codemonster.comon.global.error.problem.*;

import java.util.ArrayList;
import java.util.List;
import static site.codemonster.comon.global.error.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ProblemHighService {

    private final ProblemCollectorFactory collectorFactory;
    private final ProblemLowService problemLowService;

    public ProblemInfoResponse checkProblem(ProblemRequest problemRequest, Platform platform) {
        if (platform == Platform.PROGRAMMERS && problemRequest.title().isBlank())
            throw new ProblemInvalidInputException();

        if (problemLowService.checkDuplicateProblem(platform, problemRequest.platformProblemId())) {
            return createDuplicateResponse(platform, problemRequest, null);
        }

        return collectProblemInfo(problemRequest, platform);
    }

    public List<ProblemResponse> registerProblems(List<ProblemInfoRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new ProblemBatchRegisterException(PROBLEM_BATCH_REGISTER_EMPTY_ERROR);
        }

        List<Problem> savedProblems = new ArrayList<>();

        for (ProblemInfoRequest request : requests) {
            Problem savedProblem = saveProblem(request);
            savedProblems.add(savedProblem);
        }

        return savedProblems.stream().map(ProblemResponse::new).toList();
    }

    public void updateProblem(Long problemId, ProblemUpdateRequest problemUpdateRequest) {

        Problem problem = problemLowService.findProblemById(problemId);

        problem.updateProblem(problemUpdateRequest);
    }

    public void deleteProblem(Long problemId) {
        Problem problem = problemLowService.findProblemById(problemId);
        problemLowService.delete(problem);
    }

    private ProblemInfoResponse collectProblemInfo(ProblemRequest problemRequest, Platform platform) {
        ProblemCollector collector = collectorFactory.getCollector(platform);

        ProblemInfoRequest collectorRequest;

        if (platform == Platform.PROGRAMMERS) {
            collectorRequest = ProblemInfoRequest.builder()
                    .platform(platform)
                    .title(problemRequest.title())
                    .platformProblemId(problemRequest.platformProblemId())
                    .problemStep(problemRequest.problemStep())
                    .build();
        }
        else {
            collectorRequest = ProblemInfoRequest.builder()
                    .platform(platform)
                    .platformProblemId(problemRequest.platformProblemId())
                    .problemStep(problemRequest.problemStep())
                    .build();
        }

        try {
            return collector.collectProblemInfo(collectorRequest);
        } catch (Exception e) {
            throw new ProblemCollectionException();
        }
    }

    private ProblemInfoResponse createDuplicateResponse(Platform platform, ProblemRequest problemRequest, String originalInput) {
        String url = switch (platform) {
            case BAEKJOON -> "https://www.acmicpc.net/problem/" + problemRequest.platformProblemId();
            case PROGRAMMERS -> "https://school.programmers.co.kr/learn/courses/30/lessons/" + problemRequest.platformProblemId();
            case LEETCODE -> originalInput;
        };

        return ProblemInfoResponse.builder()
                .platform(platform)
                .platformProblemId(problemRequest.platformProblemId())
                .title("(중복된 문제)")
                .url(url)
                .isDuplicate(true)
                .success(true)
                .build();
    }

    private Problem saveProblem(ProblemInfoRequest request) {
        validateProblemRequest(request);

        Problem problem = new Problem(request);

        return problemLowService.save(problem);
    }

    private void validateProblemRequest(ProblemInfoRequest request) {
        if (request.getPlatform() == null) {
            throw new ProblemValidationException(PROBLEM_PLATFORM_REQUIRED_ERROR);
        }
        if (request.getPlatformProblemId() == null || request.getPlatformProblemId().trim().isEmpty()) {
            throw new ProblemValidationException(PROBLEM_ID_REQUIRED_ERROR);
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new ProblemValidationException(PROBLEM_TITLE_REQUIRED_ERROR);
        }
    }
}
