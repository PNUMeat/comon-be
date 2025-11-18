package site.codemonster.comon.domain.article.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.entity.ArticleFeedback;
import site.codemonster.comon.domain.article.repository.ArticleFeedbackRepository;
import site.codemonster.comon.global.error.ArticleFeedback.ArticleFeedbackNotFoundException;

@Transactional
@Service
@RequiredArgsConstructor
public class ArticleFeedbackLowService {

    private final ArticleFeedbackRepository articleFeedbackRepository;

    public ArticleFeedback save(ArticleFeedback articleFeedback) {
        return articleFeedbackRepository.save(articleFeedback);
    }

    @Transactional(readOnly = true)
    public ArticleFeedback findByArticleId(Long articleId) {
        return articleFeedbackRepository.findByArticleId(articleId)
                .orElseThrow(ArticleFeedbackNotFoundException::new);
    }

    public void deleteByArticleId(Long articleId) {
        articleFeedbackRepository.deleteByArticleId(articleId);
    }
}
