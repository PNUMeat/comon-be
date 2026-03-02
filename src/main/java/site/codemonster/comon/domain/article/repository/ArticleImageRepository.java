package site.codemonster.comon.domain.article.repository;

import site.codemonster.comon.domain.article.entity.ArticleImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleImageRepository extends JpaRepository<ArticleImage,Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ArticleImage ai WHERE ai.article.team.teamId = :teamId")
    void deleteByTeamTeamId(@Param("teamId") Long teamId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ArticleImage ai WHERE ai.article.articleId IN " +
        "(SELECT a.articleId FROM Article a WHERE a.member.id = :memberId)")
    void deleteByMemberId(@Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ArticleImage ai WHERE ai.article.articleId = :articleId")
    void deleteByArticleId(@Param("articleId") Long articleId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ArticleImage ai WHERE ai.article.articleId IN (:articleIds)")
    void deleteArticleImagesInArticleIds(@Param("articleIds") List<Long> articleIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ArticleImage ai WHERE ai.article.articleId =:articleId")
    void deleteArticleImagesInArticleId(@Param("articleId") Long articleId);
}
