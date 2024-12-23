package PNUMEAT.Backend.domain.team.dto.response;

import PNUMEAT.Backend.domain.article.dto.response.SubjectArticleDateAndTagResponse;
import PNUMEAT.Backend.domain.article.entity.Article;
import PNUMEAT.Backend.domain.team.entity.Team;
import java.util.List;
import java.util.stream.Collectors;

public record TeamPageResponse(
        MyTeamResponse myTeamResponse,
        Boolean teamManager,
        List<SubjectArticleDateAndTagResponse> subjectArticleDateAndTagResponses
) {
    public static TeamPageResponse from(Team team, Boolean teamManager , List<Article> articles){
        return new TeamPageResponse(
                MyTeamResponse.of(team),
                teamManager,
                articles.stream()
                        .map(SubjectArticleDateAndTagResponse::of)
                        .collect(Collectors.toList())
        );
    }
}
