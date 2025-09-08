package site.codemonster.comon.domain.recommendation.dto.response;

import java.util.List;

public record ManualRecommendationResponse(
        Integer totalRecommended,
        Integer processedDates,
        List<String> createdArticleTitles,
        String message
) {

    public static ManualRecommendationResponse of(int totalRecommended,
                                                  List<String> createdArticleTitles) {
        String message = String.format("%d개 날짜에 총 %d개 문제가 추천되었습니다.",
                createdArticleTitles.size(), totalRecommended);

        return new ManualRecommendationResponse(
                totalRecommended,
                createdArticleTitles.size(),
                createdArticleTitles,
                message
        );
    }

    public static ManualRecommendationResponse of(int totalRecommended,
                                                  List<String> createdArticleTitles,
                                                  String message) {
        return new ManualRecommendationResponse(
                totalRecommended,
                createdArticleTitles.size(),
                createdArticleTitles,
                message
        );
    }
}
