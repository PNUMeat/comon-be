package site.codemonster.comon.domain.problem.controller;

import static site.codemonster.comon.domain.problem.controller.ProblemResponseEnum.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.codemonster.comon.domain.problem.dto.request.ProblemBatchRequest;
import site.codemonster.comon.domain.problem.dto.request.ProblemInfoRequest;
import site.codemonster.comon.domain.problem.dto.response.ProblemInfoResponse;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.problem.service.ProblemService;
import site.codemonster.comon.global.error.dto.response.ApiResponse;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/problems")
@RequiredArgsConstructor
public class AdminProblemApiController {

    private final ProblemService problemService;

    @PostMapping("/check/baekjoon")
    public ResponseEntity<?> checkBaekjoonProblem(@RequestParam String problemId) {
        ProblemInfoResponse response = problemService.checkProblem(problemId, Platform.BAEKJOON);

        return ResponseEntity.status(PROBLEM_CHECK_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(response, PROBLEM_CHECK_SUCCESS.getMessage()));
    }

    @PostMapping("/check/programmers")
    public ResponseEntity<?> checkProgrammersProblem(@RequestBody @Valid ProblemInfoRequest request) {
        if (request.getPlatform() == null) request.setPlatform(Platform.PROGRAMMERS);

        ProblemInfoResponse response = problemService.checkProblem(request);

        return ResponseEntity.status(PROBLEM_CHECK_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(response, PROBLEM_CHECK_SUCCESS.getMessage()));
    }

    @PostMapping("/check/leetcode")
    public ResponseEntity<?> checkLeetcodeProblem(@RequestParam String url) {
        ProblemInfoResponse response = problemService.checkProblem(url, Platform.LEETCODE);

        return ResponseEntity.status(PROBLEM_CHECK_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(response, PROBLEM_CHECK_SUCCESS.getMessage()));
    }

    @PostMapping("/register/batch")
    public ResponseEntity<?> registerProblems(@RequestBody @Valid ProblemBatchRequest batchRequest) {
        List<Problem> savedProblems = problemService.registerProblems(batchRequest.getProblems());

        Map<String, Object> responseData = Map.of(
                "count", savedProblems.size(),
                "message", savedProblems.size() + "개 문제가 성공적으로 등록되었습니다."
        );

        return ResponseEntity.status(PROBLEM_BATCH_REGISTER_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.createResponse(responseData, PROBLEM_BATCH_REGISTER_SUCCESS.getMessage()));
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getProblemStatistics() {
        Map<Platform, Long> statistics = problemService.getProblemStatistics();

        return ResponseEntity.status(PROBLEM_STATISTICS_GET_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(statistics, PROBLEM_STATISTICS_GET_SUCCESS.getMessage()));
    }

    @GetMapping("/api/list")
    public ResponseEntity<?> getProblemList() {
        List<Problem> problems = problemService.getAllProblems();

        return ResponseEntity.status(PROBLEM_LIST_GET_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(problems, PROBLEM_LIST_GET_SUCCESS.getMessage()));
    }

    @GetMapping("/api/list-by-platform")
    public ResponseEntity<?> getProblemListByPlatform(@RequestParam(required = false) String platform) {
        List<Problem> problems = problemService.getProblemsByPlatform(Platform.valueOf(platform.toUpperCase()));

        return ResponseEntity.status(PROBLEM_LIST_GET_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(problems, PROBLEM_LIST_GET_SUCCESS.getMessage()));
    }

    @PutMapping("/api/{problemId}")
    public ResponseEntity<?> updateProblem(@PathVariable Long problemId, @RequestBody Map<String, String> updateData) {
        Problem updatedProblem = problemService.updateProblem(problemId, updateData);

        return ResponseEntity.status(PROBLEM_UPDATE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(updatedProblem, PROBLEM_UPDATE_SUCCESS.getMessage()));
    }

    @DeleteMapping("/api/{problemId}")
    public ResponseEntity<?> deleteProblem(@PathVariable Long problemId) {
        problemService.deleteProblem(problemId);

        return ResponseEntity.status(PROBLEM_DELETE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage(PROBLEM_DELETE_SUCCESS.getMessage()));
    }
}
