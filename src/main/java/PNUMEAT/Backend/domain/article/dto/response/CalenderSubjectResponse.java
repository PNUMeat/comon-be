package PNUMEAT.Backend.domain.article.dto.response;

import PNUMEAT.Backend.domain.article.entity.Article;
import PNUMEAT.Backend.domain.team.dto.response.MyTeamResponse;
import PNUMEAT.Backend.domain.team.entity.Team;
import java.util.List;
import java.util.stream.Collectors;

public record CalenderSubjectResponse(
        MyTeamResponse myTeamResponse,
        Boolean teamManager,
        List<SubjectArticleDateAndTagResponse> subjectArticleDateAndTagResponses
) {
    public static CalenderSubjectResponse from(Team team, Boolean teamManager , List<Article> articles){
        return new CalenderSubjectResponse(
                MyTeamResponse.of(team),
                teamManager,
                articles.stream()
                        .map(SubjectArticleDateAndTagResponse::of)
                        .collect(Collectors.toList())
        );
    }
}
