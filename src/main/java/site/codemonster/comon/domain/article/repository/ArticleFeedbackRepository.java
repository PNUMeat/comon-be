package site.codemonster.comon.domain.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.codemonster.comon.domain.article.entity.ArticleFeedback;

import java.util.Optional;

@Repository
public interface ArticleFeedbackRepository extends JpaRepository<ArticleFeedback, Long> {

    Optional<ArticleFeedback> findByArticle_ArticleId(Long articleId);

    boolean existsByArticle_ArticleId(Long articleId);

    @Query("SELECT af FROM ArticleFeedback af " +
            "JOIN FETCH af.article " +
            "WHERE af.article.articleId = :articleId")
    Optional<ArticleFeedback> findByArticleIdWithArticle(@Param("articleId") Long articleId);

    void deleteByArticle_ArticleId(Long articleId);
}
