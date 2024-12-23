package PNUMEAT.Backend.domain.article.dto.response;

import PNUMEAT.Backend.domain.article.entity.Article;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record TeamSubjectResponse(
        Long articleId,
        String articleCategory,
        String articleTitle,
        String articleBody,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdDate,
        String imageUrl
) {
    public static TeamSubjectResponse of(Article article) {
        String imageUrl = null;
        if(!article.getImages().isEmpty()){
            imageUrl = article.getImages().get(0).getImageUrl();
        }

        return new TeamSubjectResponse(
                article.getArticleId(),
                article.getArticleCategory().getName(),
                article.getArticleTitle(),
                article.getArticleBody(),
                article.getCreatedDate(),
                imageUrl
        );
    }
}
