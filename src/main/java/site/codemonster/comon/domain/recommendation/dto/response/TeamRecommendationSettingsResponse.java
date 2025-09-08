package site.codemonster.comon.domain.recommendation.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import site.codemonster.comon.domain.recommendation.entity.PlatformRecommendation;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

public record TeamRecommendationSettingsResponse(
        Long teamId,
        String teamName,
        List<PlatformRecommendationResponse> platformSettings,
        Boolean autoRecommendationEnabled,
        Integer recommendationAt,
        Set<DayOfWeek> recommendDays,
        Integer totalProblemCount
) {

    public static TeamRecommendationSettingsResponse of(TeamRecommendation teamRecommendation,
                                                        List<PlatformRecommendation> platformRecommendations,
                                                        ObjectMapper objectMapper) {
        List<PlatformRecommendationResponse> platformSettings = platformRecommendations.stream()
                .map(platform -> PlatformRecommendationResponse.of(platform, objectMapper))
                .toList();

        return new TeamRecommendationSettingsResponse(
                teamRecommendation.getTeam().getTeamId(),
                teamRecommendation.getTeam().getTeamName(),
                platformSettings,
                teamRecommendation.getAutoRecommendationEnabled(),
                teamRecommendation.getRecommendationAt(),
                teamRecommendation.getRecommendationDays(),
                teamRecommendation.getTotalProblemCount()
        );
    }
}
