package site.codemonster.comon.domain.article.dto.response;

import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.auth.entity.Member;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ArticleParticularDateResponse(
        Long articleId,
        String articleTitle,
        String articleBody,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdDate,
        String imageUrl,
        String memberName,
        String memberImage,
        Boolean isAuthor
) {
    public static ArticleParticularDateResponse from(Article article, Member member, boolean isMyTeam) {
        if(!isMyTeam){
            return new ArticleParticularDateResponse(
                    article.getArticleId(),
                    article.getArticleTitle(),
                    null,
                    article.getCreatedDate(),
                    null,
                    article.getMember().getMemberName(),
                    article.getMember().getImageUrl(),
                    member.getUuid().equals(article.getMember().getUuid())
            );
        }

        String imageUrl = null;
        if(!article.getImages().isEmpty()){
            imageUrl = article.getImages().get(0).getImageUrl();
        }

        return new ArticleParticularDateResponse(
                article.getArticleId(),
                article.getArticleTitle(),
                article.getArticleBody(),
                article.getCreatedDate(),
                imageUrl,
                article.getMember().getMemberName(),
                article.getMember().getImageUrl(),
                member.getUuid().equals(article.getMember().getUuid())
        );
    }
}
