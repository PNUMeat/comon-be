package site.codemonster.comon.domain.recommendation.controller;

import static site.codemonster.comon.domain.recommendation.controller.RecommendationResponseEnum.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.codemonster.comon.domain.recommendation.dto.response.TeamRecommendationResponse;
import site.codemonster.comon.domain.recommendation.dto.request.ManualRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.request.TeamRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.response.ManualRecommendationResponse;
import site.codemonster.comon.domain.recommendation.service.TeamRecommendationHighService;
import site.codemonster.comon.global.error.dto.response.ApiResponse;

@RestController
@RequestMapping("/admin/recommendations")
@RequiredArgsConstructor
public class AdminRecommendationApiController {

    private final TeamRecommendationHighService teamRecommendationService;

    @PostMapping("/settings")
    public ResponseEntity<Void> saveTeamRecommendationSetting(@RequestBody @Valid TeamRecommendationRequest request) {

        teamRecommendationService.saveRecommendationSettings(request);

        return ResponseEntity.status(RECOMMENDATION_SETTINGS_SAVE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON).build();
    }

    @GetMapping("/settings/{teamId}")
    public ResponseEntity<ApiResponse<TeamRecommendationResponse>> getTeamRecommendationSetting(@PathVariable Long teamId) {


        TeamRecommendationResponse response = teamRecommendationService.getRecommendationSettings(teamId);

        return ResponseEntity.status(RECOMMENDATION_SETTINGS_GET_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(response, RECOMMENDATION_SETTINGS_GET_SUCCESS.getMessage()));
    }

    @DeleteMapping("/settings/{teamId}")
    public ResponseEntity<Void> deleteTeamRecommendationSetting(@PathVariable Long teamId) {

        teamRecommendationService.deleteTeamRecommendation(teamId);


        return ResponseEntity.status(RECOMMENDATION_SETTINGS_RESET_SUCCESS.getStatusCode()).build();
    }

    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<ManualRecommendationResponse>> executeManualRecommendation(@RequestBody @Valid ManualRecommendationRequest request) {
        ManualRecommendationResponse result = teamRecommendationService.executeManualRecommendation(request);

        return ResponseEntity.status(MANUAL_RECOMMENDATION_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(result, MANUAL_RECOMMENDATION_SUCCESS.getMessage()));
    }
}
