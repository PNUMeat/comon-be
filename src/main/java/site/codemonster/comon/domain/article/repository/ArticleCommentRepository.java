package site.codemonster.comon.domain.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.codemonster.comon.domain.article.entity.ArticleComment;

import java.util.List;

public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Long> {

    @Query("SELECT c FROM ArticleComment c JOIN FETCH c.member WHERE c.article.articleId = :articleId ORDER BY c.createdDate ASC")
    List<ArticleComment> findAllByArticleIdWithMember(@Param("articleId") Long articleId);
}
