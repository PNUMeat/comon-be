package site.codemonster.comon.domain.article.dto.response;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import site.codemonster.comon.domain.article.entity.ArticleFeedback;

public record ArticleFeedbackResponse(
        Long feedbackId,
        Long articleId,
        String keyPoint,
        List<String> strengths,
        List<String> improvements,
        String learningPoint,
        LocalDateTime createdAt
) {
    public ArticleFeedbackResponse(ArticleFeedback articleFeedback) {
        this(
                articleFeedback.getFeedbackId(),
                articleFeedback.getArticle().getArticleId(),
                articleFeedback.getKeyPoint(),
                parseList(articleFeedback.getStrengths()),
                parseList(articleFeedback.getImprovements()),
                articleFeedback.getLearningPoint(),
                articleFeedback.getCreatedDate()
        );
    }

    private static List<String> parseList(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(text.split("\\|\\|\\|"));
    }
}
