package site.codemonster.comon.domain.article.dto.response;

import site.codemonster.comon.domain.article.entity.ArticleComment;

import java.time.LocalDateTime;

public record ArticleCommentResponse(
        Long commentId,
        String description,
        Long memberId,
        String memberName,
        String memberImageUrl,
        LocalDateTime createdAt,
        Boolean isDeleted
) {
    public ArticleCommentResponse(ArticleComment comment) {
        this(
                comment.getCommentId(),
                comment.getIsDeleted() ? "삭제된 댓글입니다" : comment.getDescription(),
                comment.getIsDeleted() ? null : comment.getMember().getId(),
                comment.getIsDeleted() ? null : comment.getMember().getMemberName(),
                comment.getIsDeleted() ? null : comment.getMember().getImageUrl(),
                comment.getCreatedDate(),
                comment.getIsDeleted()
        );
    }
}
