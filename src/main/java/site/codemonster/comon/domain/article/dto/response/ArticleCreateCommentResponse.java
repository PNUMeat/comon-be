package site.codemonster.comon.domain.article.dto.response;

public record ArticleCreateCommentResponse(
        Long commentId,
        Long articleOwnerId,
        String articleTitle,
        String commentDescription
) {
}
