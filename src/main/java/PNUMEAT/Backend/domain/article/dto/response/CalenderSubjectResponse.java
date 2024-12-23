package PNUMEAT.Backend.domain.article.dto.response;

import PNUMEAT.Backend.domain.article.entity.Article;
import java.util.List;
import java.util.stream.Collectors;

public record CalenderSubjectResponse(
        String teamAnnouncement,
        Boolean teamManager,
        List<SubjectArticleDateAndTagResponse> subjectArticleDateAndTagResponses
) {
    public static CalenderSubjectResponse from(String teamAnnouncement,Boolean teamManager ,List<Article> articles){
        return new CalenderSubjectResponse(
                teamAnnouncement,
                teamManager,
                articles.stream()
                        .map(SubjectArticleDateAndTagResponse::of)
                        .collect(Collectors.toList())
        );
    }
}
