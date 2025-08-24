package site.codemonster.comon.domain.recommendation.dto.response;

import java.util.List;

public record ManualRecommendationResponse(
        Integer totalRecommended,
        Integer processedDates,
        List<String> createdArticles,
        String message
) {
    public static ManualRecommendationResponse of(int totalRecommended,
                                                  int processedDates,
                                                  List<String> createdArticles) {
        String message = String.format("%d개 날짜에 총 %d개 문제가 추천되었습니다.",
                processedDates, totalRecommended);

        return new ManualRecommendationResponse(
                totalRecommended,
                processedDates,
                createdArticles,
                message
        );
    }
}
