package site.codemonster.comon.domain.recommendation.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import site.codemonster.comon.domain.recommendation.entity.PlatformRecommendation;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

public record TeamRecommendationResponse(
        Integer recommendationAt,
        Set<DayOfWeek> recommendDays,
        List<PlatformRecommendationResponse> platformRecommendationResponses
) {

}
