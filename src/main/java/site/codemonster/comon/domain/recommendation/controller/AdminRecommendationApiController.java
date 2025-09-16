package site.codemonster.comon.domain.recommendation.controller;

import static site.codemonster.comon.domain.recommendation.controller.RecommendationResponseEnum.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.recommendation.service.PlatformRecommendationService;
import site.codemonster.comon.domain.recommendation.service.TeamRecommendationCommandService;
import site.codemonster.comon.domain.recommendation.service.TeamRecommendationQueryService;
import site.codemonster.comon.domain.recommendation.dto.request.ManualRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.request.TeamRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.response.ManualRecommendationResponse;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.service.TeamService;
import site.codemonster.comon.global.error.dto.response.ApiResponse;

@RestController
@RequestMapping("/admin/recommendations")
@RequiredArgsConstructor
public class AdminRecommendationApiController {

    private final TeamRecommendationCommandService teamRecommendationCommandService;
    private final TeamRecommendationQueryService teamRecommendationQueryService;
    private final PlatformRecommendationService platformRecommendationService;
    private final TeamService teamService;

    @PostMapping("/settings")
    public ResponseEntity<?> saveTeamRecommendationSetting(@RequestBody @Valid TeamRecommendationRequest request) {
        Team team = teamService.getTeamByTeamId(request.teamId());
        TeamRecommendation teamRecommendation = teamRecommendationCommandService.getOrCreateTeamRecommendation(team);

        teamRecommendationCommandService.saveTeamRecommendationSettings(teamRecommendation, request);
        platformRecommendationService.savePlatformRecommendations(teamRecommendation, request.platformSettings());

        return ResponseEntity.status(RECOMMENDATION_SETTINGS_SAVE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage(RECOMMENDATION_SETTINGS_SAVE_SUCCESS.getMessage()));
    }

    @GetMapping("/settings/{teamId}")
    public ResponseEntity<?> getTeamRecommendationSetting(@PathVariable Long teamId) {
        Team team = teamService.getTeamByTeamId(teamId);
        TeamRecommendation teamRecommendation = teamRecommendationCommandService.getOrCreateTeamRecommendation(team);

        return ResponseEntity.status(RECOMMENDATION_SETTINGS_GET_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(teamRecommendationQueryService.getTeamRecommendationSettingsResponse(teamRecommendation), RECOMMENDATION_SETTINGS_GET_SUCCESS.getMessage()));
    }

    @PutMapping("/settings/{teamId}")
    public ResponseEntity<?> resetTeamRecommendationSetting(@PathVariable Long teamId) {
        Team team = teamService.getTeamByTeamId(teamId);

        teamRecommendationCommandService.resetTeamRecommendationSettings(team);

        return ResponseEntity.status(RECOMMENDATION_SETTINGS_RESET_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage(RECOMMENDATION_SETTINGS_RESET_SUCCESS.getMessage()));
    }

    @PostMapping("/manual")
    public ResponseEntity<?> executeManualRecommendation(@RequestBody @Valid ManualRecommendationRequest request) {
        ManualRecommendationResponse result =
                teamRecommendationCommandService.executeManualRecommendation(request);

        return ResponseEntity.status(MANUAL_RECOMMENDATION_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(result, MANUAL_RECOMMENDATION_SUCCESS.getMessage()));
    }
}
