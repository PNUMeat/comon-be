package site.codemonster.comon.domain.article.dto.response;

import site.codemonster.comon.domain.article.entity.ArticleComment;

public record ArticleCommentCreateResponse(
        Long commentId
) {
    public ArticleCommentCreateResponse(ArticleComment comment) {
        this(comment.getCommentId());
    }
}
