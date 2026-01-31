package site.codemonster.comon.domain.article.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.global.util.s3.S3ImageUtil;

public record ArticleResponse(
    Long articleId,
    String articleTitle,
    String articleBody,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdDate,
    String memberName,
    String memberImage
) {
    public ArticleResponse(Article article) {
        this(
                article.getArticleId(),
                article.getArticleTitle(),
                article.getArticleBody(),
                article.getCreatedDate(),
                article.getMember().getMemberName(),
                article.getMember().getImageUrl()
        );
    }
}
