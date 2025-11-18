package site.codemonster.comon.domain.article.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.dto.response.ArticleFeedbackResponse;
import site.codemonster.comon.domain.article.entity.ArticleFeedback;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ArticleFeedbackService {

    private final ArticleFeedbackLowService articleFeedbackLowService;

    @Transactional(readOnly = true)
    public ArticleFeedbackResponse getFeedback(Long articleId) {
        ArticleFeedback feedback = articleFeedbackLowService.findByArticleId(articleId);

        return new ArticleFeedbackResponse(feedback);
    }

    public ArticleFeedback safeSaveArticleFeedback(ArticleFeedback feedback) {
        articleFeedbackLowService.deleteByArticleId(feedback.getArticle().getArticleId());

        return articleFeedbackLowService.save(feedback);
    }

}
