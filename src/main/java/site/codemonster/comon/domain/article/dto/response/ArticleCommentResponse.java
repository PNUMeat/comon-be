package site.codemonster.comon.domain.article.dto.response;

import site.codemonster.comon.domain.article.entity.ArticleComment;

import java.time.LocalDateTime;

public record ArticleCommentResponse(
        Long commentId,
        String description,
        Long memberId,
        String memberName,
        LocalDateTime createdAt
) {
    public ArticleCommentResponse(ArticleComment comment) {
        this(
                comment.getCommentId(),
                comment.getDescription(),
                comment.getMember().getId(),
                comment.getMember().getMemberName(),
                comment.getCreatedDate()
        );
    }
}
