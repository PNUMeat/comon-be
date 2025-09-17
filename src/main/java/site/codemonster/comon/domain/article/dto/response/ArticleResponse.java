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
    String memberName,
    String memberImage
) {
    public static ArticleResponse of(Article article) {
        return new ArticleResponse(
            article.getArticleId(),
            article.getArticleTitle(),
            article.getArticleBody(),
            article.getCreatedDate(),
            article.getMember().getMemberName(),
            article.getMember().getImageUrl()
        );
    }
}
