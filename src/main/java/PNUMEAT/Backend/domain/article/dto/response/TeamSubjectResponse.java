package PNUMEAT.Backend.domain.article.dto.response;

import PNUMEAT.Backend.domain.article.entity.Article;

import java.time.LocalDateTime;

public record TeamSubjectResponse(
        Long articleId,
        String articleCategory,
        String articleTitle,
        String articleBody,
        LocalDateTime updatedDate,
        String imageUrl
) {
    public static TeamSubjectResponse of(Article article) {
        return new TeamSubjectResponse(
                article.getArticleId(),
                article.getArticleCategory().getName(),
                article.getArticleTitle(),
                article.getArticleBody(),
                article.getUpdatedDate(),
                article.getImages().get(0).getImageUrl()
        );
    }
}
