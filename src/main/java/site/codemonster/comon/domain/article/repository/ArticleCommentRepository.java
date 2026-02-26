package site.codemonster.comon.domain.article.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.codemonster.comon.domain.article.entity.ArticleComment;

public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Long> {

    @Query("SELECT comment FROM ArticleComment comment " +
            "JOIN FETCH comment.member " +
            "WHERE comment.article.articleId = :articleId")
    Page<ArticleComment> findActiveCommentsByArticleId(@Param("articleId") Long articleId, Pageable pageable);
}
