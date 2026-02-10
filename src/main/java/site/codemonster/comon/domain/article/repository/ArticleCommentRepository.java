package site.codemonster.comon.domain.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.codemonster.comon.domain.article.entity.ArticleComment;

public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Long> {
}
