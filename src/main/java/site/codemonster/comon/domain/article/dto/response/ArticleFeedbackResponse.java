package site.codemonster.comon.domain.article.dto.response;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import site.codemonster.comon.domain.article.entity.ArticleFeedback;

public record ArticleFeedbackResponse(
        Long feedbackId,
        Long articleId,
        String feedbackBody
) {

    public ArticleFeedbackResponse(ArticleFeedback articleFeedback) {
        this(articleFeedback.getFeedbackId(), articleFeedback.getArticle().getArticleId()
        , articleFeedback.getFeedbackBody());
    }
}
