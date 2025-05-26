package site.codemonster.comon.domain.article.dto.response;

import site.codemonster.comon.domain.article.entity.Article;

import java.time.LocalDate;

public record SubjectArticleDateAndTagResponse(
        LocalDate subjectDate,
        String articleCategory
) {
    public static SubjectArticleDateAndTagResponse of(Article article){
        return new SubjectArticleDateAndTagResponse(article.getSelectedDate(), article.getArticleCategory().getName());
    }
}
