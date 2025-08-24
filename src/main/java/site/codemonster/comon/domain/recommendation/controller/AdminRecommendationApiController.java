package site.codemonster.comon.domain.recommendation.controller;

import static site.codemonster.comon.domain.recommendation.controller.RecommendationResponseEnum.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.codemonster.comon.domain.recommendation.service.TeamRecommendationService;
import site.codemonster.comon.domain.recommendation.dto.request.ManualRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.request.TeamRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.response.ManualRecommendationResponse;
import site.codemonster.comon.domain.recommendation.dto.response.TeamRecommendationSettingsResponse;
import site.codemonster.comon.global.error.dto.response.ApiResponse;

@RestController
@RequestMapping("/admin/recommendations")
@RequiredArgsConstructor
public class AdminRecommendationApiController {

    private final TeamRecommendationService recommendationService;

    @PostMapping("/settings")
    public ResponseEntity<?> saveTeamRecommendationSettings(@RequestBody @Valid TeamRecommendationRequest request) {
        recommendationService.saveRecommendationSettings(request);

        return ResponseEntity.status(RECOMMENDATION_SETTINGS_SAVE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage(RECOMMENDATION_SETTINGS_SAVE_SUCCESS.getMessage()));
    }

    @GetMapping("/settings/{teamId}")
    public ResponseEntity<?> getTeamRecommendationSettings(@PathVariable Long teamId) {
        TeamRecommendationSettingsResponse settings =
                recommendationService.getRecommendationSettings(teamId);

        return ResponseEntity.status(RECOMMENDATION_SETTINGS_GET_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(settings, RECOMMENDATION_SETTINGS_GET_SUCCESS.getMessage()));
    }

    @DeleteMapping("/settings/{teamId}")
    public ResponseEntity<?> resetTeamRecommendationSettings(@PathVariable Long teamId) {
        recommendationService.resetRecommendationSettings(teamId);

        return ResponseEntity.status(RECOMMENDATION_SETTINGS_RESET_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage(RECOMMENDATION_SETTINGS_RESET_SUCCESS.getMessage()));
    }

    @PostMapping("/manual")
    public ResponseEntity<?> executeManualRecommendation(@RequestBody @Valid ManualRecommendationRequest request) {
        ManualRecommendationResponse result =
                recommendationService.executeManualRecommendation(request);

        return ResponseEntity.status(MANUAL_RECOMMENDATION_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(result, MANUAL_RECOMMENDATION_SUCCESS.getMessage()));
    }
}
