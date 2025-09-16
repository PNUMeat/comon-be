package site.codemonster.comon.domain.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.recommendation.dto.response.TeamRecommendationSettingsResponse;
import site.codemonster.comon.domain.recommendation.entity.PlatformRecommendation;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.recommendation.repository.TeamRecommendationRepository;
import site.codemonster.comon.domain.team.entity.Team;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRecommendationQueryService {
    private final TeamRecommendationRepository teamRecommendationRepository;
    private final PlatformRecommendationService platformRecommendationService;
    private final ObjectMapper objectMapper;

    public Optional<TeamRecommendation> getTeamRecommendationByTeam(Team team) {
        return teamRecommendationRepository.findTeamRecommendationByTeam(team);
    }

    public TeamRecommendationSettingsResponse getTeamRecommendationSettingsResponse(TeamRecommendation teamRecommendation) {
        List<PlatformRecommendation> platformRecommendations =
                platformRecommendationService.findByTeamRecommendation(teamRecommendation);
        return TeamRecommendationSettingsResponse.of(teamRecommendation, platformRecommendations, objectMapper);
    }

    public List<TeamRecommendation> getSchedulingActiveTeamRecommendations() {
        return teamRecommendationRepository.findByAutoRecommendationEnabledTrue();
    }
}
