package site.codemonster.comon.domain.article.dto.response;

import site.codemonster.comon.domain.article.entity.ArticleComment;

public record ArticleCommentIdResponse(
        Long commentId
) {
    public ArticleCommentIdResponse(ArticleComment comment) {
        this(comment.getCommentId());
    }
}
