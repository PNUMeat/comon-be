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

        String imageUrl = null;
        if(!article.getImages().isEmpty()){
            imageUrl = article.getImages().get(0).getImageUrl();
        }

        TeamSubjectResponse teamSubjectResponse = new TeamSubjectResponse(
                article.getArticleId(),
                article.getArticleCategory().getName(),
                article.getArticleTitle(),
                article.getArticleBody(),
                article.getUpdatedDate(),
                imageUrl
        );

        return teamSubjectResponse;
    }
}
