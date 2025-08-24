package site.codemonster.comon.domain.problem.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.codemonster.comon.domain.problem.dto.request.ProblemBatchRequest;
import site.codemonster.comon.domain.problem.dto.request.ProblemInfoRequest;
import site.codemonster.comon.domain.problem.dto.response.ProblemInfoResponse;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.problem.service.ProblemService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/problems")
@RequiredArgsConstructor
public class AdminProblemApiController {

    private final ProblemService problemService;

    @PostMapping("/check/baekjoon")
    public Map<String, Object> checkBaekjoonProblem(@RequestParam String problemId) {
        try {
            ProblemInfoResponse response = problemService.checkProblem(problemId, Platform.BAEKJOON);
            return toResponseMap(response);
        } catch (Exception e) {
            return Map.of("success", false, "errorMessage", "문제 정보를 찾을 수 없습니다: " + e.getMessage());
        }
    }

    @PostMapping("/check/programmers")
    public Map<String, Object> checkProgrammersProblem(@RequestBody ProblemInfoRequest request) {
        try {
            if (request.getPlatform() == null) {
                request.setPlatform(Platform.PROGRAMMERS);
            }
            ProblemInfoResponse response = problemService.checkProblem(request);
            return toResponseMap(response);
        } catch (Exception e) {
            return Map.of("success", false, "errorMessage", "입력 정보를 확인해주세요: " + e.getMessage());
        }
    }

    @PostMapping("/check/leetcode")
    public Map<String, Object> checkLeetcodeProblem(@RequestParam String url) {
        try {
            ProblemInfoResponse response = problemService.checkProblem(url, Platform.LEETCODE);
            return toResponseMap(response);
        } catch (Exception e) {
            return Map.of("success", false, "errorMessage", "문제 정보를 찾을 수 없습니다: " + e.getMessage());
        }
    }

    @PostMapping("/register/batch")
    public Map<String, Object> registerProblems(@RequestBody ProblemBatchRequest batchRequest) {
        try {
            List<Problem> savedProblems = problemService.registerProblems(batchRequest.getProblems());
            return Map.of(
                    "success", true,
                    "message", savedProblems.size() + "개 문제가 성공적으로 등록되었습니다.",
                    "count", savedProblems.size()
            );
        } catch (Exception e) {
            return Map.of("success", false, "message", "문제 등록에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public Map<Platform, Long> getProblemStatistics() {
        return problemService.getProblemStatistics();
    }

    @GetMapping("/api/list")
    public List<Map<String, Object>> getProblemList() {
        try {
            return problemService.getAllProblems()
                    .stream()
                    .map(this::toProblemMap)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @GetMapping("/api/list-by-platform")
    public List<Map<String, Object>> getProblemListByPlatform(@RequestParam(required = false) String platform) {
        try {
            if (platform == null || platform.isEmpty()) {
                return Collections.emptyList();
            }

            List<Problem> problems = problemService.getProblemsByPlatform(Platform.valueOf(platform.toUpperCase()));

            return problems.stream()
                    .map(this::toProblemMap)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @PutMapping("/api/{problemId}")
    public Map<String, Object> updateProblem(@PathVariable Long problemId, @RequestBody Map<String, String> updateData) {
        try {
            Problem updatedProblem = problemService.updateProblem(problemId, updateData);
            return Map.of(
                    "success", true,
                    "message", "문제가 성공적으로 수정되었습니다.",
                    "problem", updatedProblem
            );
        } catch (Exception e) {
            return Map.of("success", false, "message", "문제 수정에 실패했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/api/{problemId}")
    public Map<String, Object> deleteProblem(@PathVariable Long problemId) {
        try {
            problemService.deleteProblem(problemId);
            return Map.of("success", true, "message", "문제가 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "문제 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    private Map<String, Object> toResponseMap(ProblemInfoResponse response) {
        return Map.of(
                "success", response.isSuccess(),
                "isDuplicate", response.isDuplicate(),
                "platform", response.getPlatform().name(),
                "platformProblemId", response.getPlatformProblemId(),
                "title", response.getTitle(),
                "difficulty", response.getDifficulty(),
                "url", response.getUrl(),
                "tags", response.getTags()
        );
    }

    private Map<String, Object> toProblemMap(Problem problem) {
        return Map.of(
                "problemId", problem.getProblemId(),
                "platform", problem.getPlatform().name(),
                "platformProblemId", problem.getPlatformProblemId(),
                "title", problem.getTitle(),
                "difficulty", problem.getDifficulty(),
                "url", problem.getUrl(),
                "tags", problem.getTags(),
                "createdAt", problem.getCreatedDate() != null ? problem.getCreatedDate().toString() : null,
                "updatedAt", problem.getUpdatedDate() != null ? problem.getUpdatedDate().toString() : null
        );
    }
}
