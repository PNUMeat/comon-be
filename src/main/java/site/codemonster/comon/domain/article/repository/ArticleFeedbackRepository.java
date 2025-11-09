package site.codemonster.comon.domain.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.codemonster.comon.domain.article.entity.ArticleFeedback;

import java.util.Optional;

@Repository
public interface ArticleFeedbackRepository extends JpaRepository<ArticleFeedback, Long> {

    Optional<ArticleFeedback> findByArticle_ArticleId(Long articleId);
}
