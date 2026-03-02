package site.codemonster.comon.domain.article.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.codemonster.comon.domain.article.entity.ArticleComment;

import java.util.List;

public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Long> {

    @Query("SELECT comment FROM ArticleComment comment " +
            "LEFT JOIN FETCH comment.member " +
            "WHERE comment.article.articleId = :articleId")
    Page<ArticleComment> findActiveCommentsByArticleId(@Param("articleId") Long articleId, Pageable pageable);



    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ArticleComment comment where comment.article.articleId in :articleIds")
    void deleteByArticleIds(List<Long> articleIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ArticleComment comment where comment.article.articleId = :articleId")
    void deleteByArticleId(Long articleId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ArticleComment comment set comment.isDeleted = true, comment.member = null where comment.member.id = :memberId")
    void sofDeleteByMemberId(Long memberId);
}

