package site.codemonster.comon.domain.article.dto.response;

import site.codemonster.comon.domain.article.entity.Article;

public record ArticleCreateResponse(
        Long articleId
) {
    public ArticleCreateResponse(Article article){
        this (
                article.getArticleId()
        );
    }
}
