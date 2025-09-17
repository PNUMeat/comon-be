package site.codemonster.comon.domain.article.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import site.codemonster.comon.domain.article.dto.response.ArticleParticularDateResponse;
import site.codemonster.comon.domain.article.dto.response.ArticleResponse;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.global.util.convertUtils.ImageFieldConvertUtils;

@Component
@RequiredArgsConstructor
public class ArticleResponseUtils {
    private final ImageFieldConvertUtils imageFieldConvertUtils;

    public ArticleResponse getArticleResponse(Article article){
        return new ArticleResponse(
                article.getArticleId(),
                article.getArticleTitle(),
                article.getArticleBody(),
                article.getCreatedDate(),
                article.getMember().getMemberName(),
                imageFieldConvertUtils.convertObjectKeyToImageUrl(article.getMember().getImageUrl())  // 변환된 멤버 이미지
        );
    }

    public ArticleParticularDateResponse getArticleParticularDateResponse(Article article, Member member, boolean isMyTeam) {
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
                imageFieldConvertUtils.convertObjectKeyToImageUrl(article.getMember().getImageUrl()), // 변환!
                member.getUuid().equals(article.getMember().getUuid())
        );
    }
}
