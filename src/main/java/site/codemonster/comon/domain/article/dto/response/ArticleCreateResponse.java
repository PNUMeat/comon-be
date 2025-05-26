package site.codemonster.comon.domain.article.dto.response;

import site.codemonster.comon.domain.article.entity.Article;

public record ArticleCreateResponse(
        Long articleId
) {
    public static ArticleCreateResponse of(Article article){
        return new ArticleCreateResponse(article.getArticleId());
    }
}
