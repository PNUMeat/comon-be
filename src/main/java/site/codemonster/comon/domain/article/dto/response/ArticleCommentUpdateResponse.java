package site.codemonster.comon.domain.article.dto.response;

import site.codemonster.comon.domain.article.entity.ArticleComment;

public record ArticleCommentUpdateResponse(
        Long commentId
) {
    public ArticleCommentUpdateResponse(ArticleComment comment) {
        this(comment.getCommentId());
    }
}
