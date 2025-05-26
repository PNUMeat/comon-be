package site.codemonster.comon.domain.article.dto.response;

import site.codemonster.comon.domain.article.entity.Article;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ArticleResponse(
    Long articleId,
    String articleTitle,
    String articleBody,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdDate,
    String imageUrl,
    String memberName,
    String memberImage
) {
    public static ArticleResponse of(Article article) {
        String imageUrl = null;
        if(!article.getImages().isEmpty()){
            imageUrl = article.getImages().get(0).getImageUrl();
        }

        return new ArticleResponse(
            article.getArticleId(),
            article.getArticleTitle(),
            article.getArticleBody(),
            article.getCreatedDate(),
            imageUrl,
            article.getMember().getMemberName(),
            article.getMember().getImageUrl()
        );
    }
}
