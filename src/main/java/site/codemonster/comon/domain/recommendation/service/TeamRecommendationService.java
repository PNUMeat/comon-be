package site.codemonster.comon.domain.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.recommendation.dto.request.TeamRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.response.TeamRecommendationSettingsResponse;
import site.codemonster.comon.domain.recommendation.entity.PlatformRecommendation;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.recommendation.repository.TeamRecommendationRepository;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.service.TeamService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRecommendationService {

    private final TeamService teamService;
    private final TeamRecommendationRepository teamRecommendationRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveRecommendationSettings(TeamRecommendationRequest request) {
        Team team = teamService.getTeamByTeamId(request.teamId());

        TeamRecommendation teamRecommendation = findOrCreateTeamRecommendation(team);
        updateTeamRecommendation(teamRecommendation, request);

        teamRecommendationRepository.save(teamRecommendation);
    }

    public TeamRecommendationSettingsResponse getRecommendationSettings(Long teamId) {
        // 팀 검증
        Team team = teamService.getTeamByTeamId(teamId);

        // 추천 설정 조회 (없으면 기본값 반환)
        TeamRecommendation teamRecommendation = teamRecommendationRepository
                .findByTeamIdWithPlatforms(teamId)
                .orElse(createDefaultTeamRecommendation(team));

        // DTO 변환해서 반환
        return TeamRecommendationSettingsResponse.of(teamRecommendation, objectMapper);
    }

    @Transactional
    public void resetRecommendationSettings(Long teamId) {
        Team team = teamService.getTeamByTeamId(teamId);

        teamRecommendationRepository.findByTeam(team)
                .ifPresentOrElse(
                        TeamRecommendation::resetRecommendationSetting,
                        () -> {
                            TeamRecommendation newRecommendation = createDefaultTeamRecommendation(team);
                            teamRecommendationRepository.save(newRecommendation);
                        }
                );
    }

    private TeamRecommendation findOrCreateTeamRecommendation(Team team) {
        return teamRecommendationRepository.findByTeam(team)
                .orElse(createDefaultTeamRecommendation(team));
    }

    private TeamRecommendation createDefaultTeamRecommendation(Team team) {
        return TeamRecommendation.builder()
                .team(team)
                .autoRecommendationEnabled(false)
                .recommendationAt(9)
                .totalProblemCount(0)
                .build();
    }

    private void updateTeamRecommendation(TeamRecommendation teamRecommendation, TeamRecommendationRequest request) {
        teamRecommendation.setAutoRecommendationEnabled(request.autoRecommendationEnabled());
        teamRecommendation.setRecommendationAt(request.recommendationAt());
        teamRecommendation.setTotalProblemCount(calculateTotalProblemCount(request.platformSettings()));
        teamRecommendation.setRecommendationDays(request.recommendDays());

        List<PlatformRecommendation> platformRecommendations = request.platformSettings().stream()
                .map(this::createPlatformRecommendation)
                .collect(Collectors.toList());

        teamRecommendation.replacePlatformRecommendations(platformRecommendations);
    }

    private PlatformRecommendation createPlatformRecommendation(TeamRecommendationRequest.PlatformRecommendationSetting setting) {
        return PlatformRecommendation.builder()
                .platform(setting.platform())
                .difficulties(convertListToJson(setting.difficulties()))
                .tags(convertListToJson(setting.tags()))
                .problemCount(setting.problemCount())
                .enabled(setting.enabled())
                .build();
    }

    private Integer calculateTotalProblemCount(List<TeamRecommendationRequest.PlatformRecommendationSetting> settings) {
        return settings.stream()
                .filter(TeamRecommendationRequest.PlatformRecommendationSetting::enabled)
                .mapToInt(TeamRecommendationRequest.PlatformRecommendationSetting::problemCount)
                .sum();
    }

    private String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }
}
