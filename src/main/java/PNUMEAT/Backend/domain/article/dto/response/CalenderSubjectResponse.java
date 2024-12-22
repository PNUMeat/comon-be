package PNUMEAT.Backend.domain.article.dto.response;

import PNUMEAT.Backend.domain.article.entity.Article;
import java.util.List;
import java.util.stream.Collectors;

public record CalenderSubjectResponse(
        String teamAnnouncement,
        List<SubjectArticleDateAndTagResponse> subjectArticleDateAndTagResponses
) {
    public static CalenderSubjectResponse of(String teamAnnouncement, List<Article> articles){
        return new CalenderSubjectResponse(
                teamAnnouncement,
                articles.stream()
                        .map(SubjectArticleDateAndTagResponse::of)
                        .collect(Collectors.toList())
        );
    }
}
