package site.codemonster.comon.domain.recommendation.dto.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.recommendation.entity.PlatformRecommendation;

import java.util.Collections;
import java.util.List;

public record PlatformRecommendationResponse(
        Platform platform,
        List<String> difficulties,
        List<String> tags,
        Integer problemCount,
        Boolean enabled
) {

    public static PlatformRecommendationResponse of(PlatformRecommendation platformRecommendation, ObjectMapper objectMapper) {
        return new PlatformRecommendationResponse(
                platformRecommendation.getPlatform(),
                parseJson(platformRecommendation.getDifficulties(), objectMapper),
                parseJson(platformRecommendation.getTags(), objectMapper),
                platformRecommendation.getProblemCount(),
                platformRecommendation.getEnabled()
        );
    }

    private static List<String> parseJson(String json, ObjectMapper objectMapper) {
        if (json == null || json.trim().equals("[]")) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
