package PNUMEAT.Backend.domain.article.dto.response;

import PNUMEAT.Backend.domain.article.entity.Article;
import PNUMEAT.Backend.domain.article.enums.ArticleCategory;
import java.time.LocalDate;

public record SubjectArticleDateAndTagResponse(
        LocalDate subjectDate,
        ArticleCategory articleCategory
) {
    public static SubjectArticleDateAndTagResponse of(Article article){
        return new SubjectArticleDateAndTagResponse(article.getSelectedDate(), article.getArticleCategory());
    }
}
