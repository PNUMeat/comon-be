package site.codemonster.comon.domain.problem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.problem.collector.ProblemCollector;
import site.codemonster.comon.domain.problem.collector.ProblemCollectorFactory;
import site.codemonster.comon.domain.problem.dto.request.ProblemInfoRequest;
import site.codemonster.comon.domain.problem.dto.response.ProblemInfoResponse;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.problem.repository.ProblemRepository;
import site.codemonster.comon.global.error.problem.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static site.codemonster.comon.global.error.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ProblemCommandService {

    private final ProblemRepository problemRepository;
    private final ProblemCollectorFactory collectorFactory;
    private final ProblemQueryService problemQueryService;

    public ProblemInfoResponse checkProblem(String problemId, Platform platform) {
        if (problemQueryService.checkDuplicateProblem(platform, problemId)) {
            return createDuplicateResponse(platform, problemId, problemId);
        }

        return collectProblemInfo(problemId, platform);
    }

    public ProblemInfoResponse checkProblem(ProblemInfoRequest request) {
        Platform platform = request.getPlatform();
        String problemId = request.getPlatformProblemId();

        if (problemQueryService.checkDuplicateProblem(platform, problemId)) {
            return createDuplicateResponse(platform, problemId, null);
        }

        return collectProblemInfoFromRequest(request);
    }

    public List<Problem> registerProblems(List<ProblemInfoRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new ProblemBatchRegisterException(PROBLEM_BATCH_REGISTER_EMPTY_ERROR);
        }

        List<Problem> savedProblems = new ArrayList<>();

        for (ProblemInfoRequest request : requests) {
            if (problemQueryService.checkDuplicateProblem(request.getPlatform(), request.getPlatformProblemId())) {
                continue;
            }

            Problem savedProblem = saveProblem(request);
            savedProblems.add(savedProblem);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ProblemBatchRegisterException(PROBLEM_REGISTER_INTERRUPTED_ERROR);
            }
        }

        if (savedProblems.isEmpty()) {
            throw new ProblemBatchRegisterException(PROBLEM_ALL_DUPLICATED_ERROR);
        }

        return savedProblems;
    }

    public Problem updateProblem(Long problemId, Map<String, String> updateData) {
        if (updateData == null || updateData.isEmpty()) {
            throw new ProblemValidationException(PROBLEM_UPDATE_DATA_EMPTY_ERROR);
        }

        Problem problem = problemQueryService.findProblemById(problemId);
        updateProblemFields(problem, updateData);

        return problemRepository.save(problem);
    }

    public void deleteProblem(Long problemId) {
        Problem problem = problemQueryService.findProblemById(problemId);
        problemRepository.delete(problem);
    }

    private ProblemInfoResponse collectProblemInfo(String problemInput, Platform platform) {
        ProblemCollector collector = collectorFactory.getCollector(platform);

        ProblemInfoRequest collectorRequest = ProblemInfoRequest.builder()
                .platform(platform)
                .platformProblemId(problemInput)
                .build();

        try {
            return collector.collectProblemInfo(collectorRequest);
        } catch (Exception e) {
            throw new ProblemCollectionException();
        }
    }

    private ProblemInfoResponse collectProblemInfoFromRequest(ProblemInfoRequest request) {
        ProblemCollector collector = collectorFactory.getCollector(request.getPlatform());

        if (!collector.isValidProblemId(request.getPlatformProblemId())) {
            throw new ProblemInvalidInputException();
        }

        try {
            return collector.collectProblemInfo(request);
        } catch (Exception e) {
            throw new ProblemCollectionException();
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

    private Problem saveProblem(ProblemInfoRequest request) {
        validateProblemRequest(request);

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

    private void updateProblemFields(Problem problem, Map<String, String> updateData) {
        updateData.forEach((key, value) -> {
            switch (key) {
                case "title" -> {
                    if (value == null || value.trim().isEmpty()) {
                        throw new ProblemValidationException(PROBLEM_TITLE_REQUIRED_ERROR);
                    }
                    problem.setTitle(value.trim());
                }
                case "difficulty" -> problem.setDifficulty(value != null ? value.trim() : null);
                case "tags" -> problem.setTags(value != null ? value.trim() : null);
                case "url" -> problem.setUrl(value != null ? value.trim() : null);
                default -> throw new ProblemValidationException(PROBLEM_UNSUPPORTED_FIELD_ERROR);
            }
        });
    }
}
