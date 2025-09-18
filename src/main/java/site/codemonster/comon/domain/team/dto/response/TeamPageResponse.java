package site.codemonster.comon.domain.team.dto.response;

import site.codemonster.comon.domain.article.dto.response.SubjectArticleDateAndTagResponse;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.team.entity.Team;

import java.util.List;
import java.util.stream.Collectors;

public record TeamPageResponse(
        MyTeamResponse myTeamResponse,
        Boolean teamManager,
        List<SubjectArticleDateAndTagResponse> subjectArticleDateAndTagResponses
) {
    public static TeamPageResponse from(MyTeamResponse myTeamResponse, Boolean teamManager , List<Article> articles){
        return new TeamPageResponse(
                myTeamResponse,
                teamManager,
                articles.stream()
                        .map(SubjectArticleDateAndTagResponse::of)
                        .collect(Collectors.toList())
        );
    }
}
