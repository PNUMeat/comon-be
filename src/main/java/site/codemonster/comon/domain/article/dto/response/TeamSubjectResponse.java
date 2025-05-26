package site.codemonster.comon.domain.article.dto.response;

import site.codemonster.comon.domain.article.entity.Article;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record TeamSubjectResponse(
        Long articleId,
        String articleCategory,
        String articleTitle,
        String articleBody,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdDate,
        String imageUrl,
        String authorName,
        String authorImageUrl
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
                imageUrl,
                article.getMember().getMemberName(),
                article.getMember().getImageUrl()
        );
    }
}
