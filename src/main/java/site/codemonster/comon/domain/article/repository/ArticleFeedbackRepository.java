package site.codemonster.comon.domain.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.entity.ArticleFeedback;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleFeedbackRepository extends JpaRepository<ArticleFeedback, Long> {

    @Query("select af from ArticleFeedback af where af.article.articleId = :articleId")
    Optional<ArticleFeedback> findByArticleId(Long articleId);

    boolean existsByArticle(Article article);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ArticleFeedback af where af.article.articleId = :articleId")
    void deleteByArticleId(Long articleId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ArticleFeedback af where af.article.articleId in :articleIds")
    void deleteByArticleIds(List<Long> articleIds);
}
