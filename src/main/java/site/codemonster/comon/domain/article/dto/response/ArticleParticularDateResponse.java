package site.codemonster.comon.domain.article.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.global.util.s3.S3ImageUtil;

public record ArticleParticularDateResponse(
        Long articleId,
        String articleTitle,
        String articleBody,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdDate,
        String memberName,
        String memberImage,
        Boolean isAuthor
) {
    public static ArticleParticularDateResponse of(Article article, Member member, boolean isMyTeam) {
        if (!isMyTeam) {
            return new ArticleParticularDateResponse(
                    article.getArticleId(),
                    article.getArticleTitle(),
                    null,
                    article.getCreatedDate(),
                    null,
                    article.getMember().getMemberName(),
                    member.getUuid().equals(article.getMember().getUuid())
            );
        }

        return new ArticleParticularDateResponse(
                article.getArticleId(),
                article.getArticleTitle(),
                article.getArticleBody(),
                article.getCreatedDate(),
                article.getMember().getMemberName(),
                article.getMember().getImageUrl(),
                member.getUuid().equals(article.getMember().getUuid())
        );
    }
}
