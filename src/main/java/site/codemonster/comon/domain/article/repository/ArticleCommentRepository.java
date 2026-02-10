package site.codemonster.comon.domain.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.codemonster.comon.domain.article.entity.ArticleComment;

import java.util.List;

public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Long> {

    @Query("SELECT comment FROM ArticleComment comment " +
            "JOIN FETCH comment.member " +
            "WHERE comment.article.articleId = :articleId " +
            "ORDER BY comment.createdDate ASC")
    List<ArticleComment> findAllByArticleIdWithMember(@Param("articleId") Long articleId);
}
