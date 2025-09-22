package site.codemonster.comon.domain.problem.controller;

import static site.codemonster.comon.domain.problem.controller.ProblemResponseEnum.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.codemonster.comon.domain.problem.dto.request.ProblemBatchRequest;
import site.codemonster.comon.domain.problem.dto.request.ProblemRequest;
import site.codemonster.comon.domain.problem.dto.request.ProblemUpdateRequest;
import site.codemonster.comon.domain.problem.dto.response.ProblemInfoResponse;
import site.codemonster.comon.domain.problem.dto.response.ProblemResponse;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.problem.service.ProblemHighService;
import site.codemonster.comon.domain.problem.service.ProblemLowService;
import site.codemonster.comon.global.error.dto.response.ApiResponse;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/problems")
@RequiredArgsConstructor
public class AdminProblemApiController {

    private final ProblemHighService problemCommandService;
    private final ProblemLowService problemQueryService;

    @PostMapping("/get/baekjoon")
    public ResponseEntity<ApiResponse<ProblemInfoResponse>> getBaekjoonProblemInfo(@RequestBody @Valid ProblemRequest problemRequest) {
        ProblemInfoResponse response = problemCommandService.checkProblem(problemRequest, Platform.BAEKJOON);

        return ResponseEntity.status(PROBLEM_CHECK_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(response, PROBLEM_CHECK_SUCCESS.getMessage()));
    }

    @PostMapping("/get/programmers")
    public ResponseEntity<ApiResponse<ProblemInfoResponse>> getProgrammersProblemInfo(@RequestBody @Valid ProblemRequest problemRequest) {

        ProblemInfoResponse response = problemCommandService.checkProblem(problemRequest, Platform.PROGRAMMERS);

        return ResponseEntity.status(PROBLEM_CHECK_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(response, PROBLEM_CHECK_SUCCESS.getMessage()));
    }

    @PostMapping("/get/leetcode")
    public ResponseEntity<ApiResponse<ProblemInfoResponse>> getLeetcodeProblemInfo(@RequestBody @Valid ProblemRequest problemRequest) {
        ProblemInfoResponse response = problemCommandService.checkProblem(problemRequest, Platform.LEETCODE);

        return ResponseEntity.status(PROBLEM_CHECK_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(response, PROBLEM_CHECK_SUCCESS.getMessage()));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String,Object>>> registerProblems(@RequestBody @Valid ProblemBatchRequest batchRequest) {
        List<ProblemResponse> savedProblems = problemCommandService.registerProblems(batchRequest.getProblems());

        Map<String, Object> responseData = Map.of(
                "count", savedProblems.size(),
                "message", savedProblems.size() + "개 문제가 성공적으로 등록되었습니다."
        );

        return ResponseEntity.status(PROBLEM_BATCH_REGISTER_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.createResponse(responseData, PROBLEM_BATCH_REGISTER_SUCCESS.getMessage()));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<Platform, Long>>> getProblemStatistics() {
        Map<Platform, Long> statistics = problemQueryService.getProblemStatistics();

        return ResponseEntity.status(PROBLEM_STATISTICS_GET_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(statistics, PROBLEM_STATISTICS_GET_SUCCESS.getMessage()));
    }

    @GetMapping("/problem-list")
    public ResponseEntity<ApiResponse<List<ProblemResponse>>> getProblemList() {
        List<ProblemResponse> problems = problemQueryService.getAllProblems();

        return ResponseEntity.status(PROBLEM_LIST_GET_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(problems, PROBLEM_LIST_GET_SUCCESS.getMessage()));
    }

    @GetMapping("/list-by-platform")
    public ResponseEntity<ApiResponse<List<ProblemResponse>>> getProblemListByPlatform(@RequestParam String platform) {
        List<ProblemResponse> problems = problemQueryService.getProblemsByPlatform(Platform.valueOf(platform.toUpperCase()));

        return ResponseEntity.status(PROBLEM_LIST_GET_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(problems, PROBLEM_LIST_GET_SUCCESS.getMessage()));
    }

    @PutMapping("/{problemId}")
    public ResponseEntity<ApiResponse<Void>> updateProblem(@PathVariable Long problemId, @RequestBody @Valid ProblemUpdateRequest problemUpdateRequest) {
        problemCommandService.updateProblem(problemId, problemUpdateRequest);

        return ResponseEntity.status(PROBLEM_UPDATE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage(PROBLEM_UPDATE_SUCCESS.getMessage()));
    }

    @DeleteMapping("/{problemId}")
    public ResponseEntity<ApiResponse<Void>> deleteProblem(@PathVariable Long problemId) {
        problemCommandService.deleteProblem(problemId);

        return ResponseEntity.status(PROBLEM_DELETE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage(PROBLEM_DELETE_SUCCESS.getMessage()));
    }
}
