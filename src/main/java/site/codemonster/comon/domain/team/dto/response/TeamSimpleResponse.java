package site.codemonster.comon.domain.team.dto.response;

import site.codemonster.comon.domain.team.entity.Team;

public record TeamSimpleResponse(Long teamId,
                                 String teamName,
                                 int memberCount) {

    public static TeamSimpleResponse of(Team team) {
        return new TeamSimpleResponse(
                team.getTeamId(),
                team.getTeamName(),
                team.getTeamMembers().size()
        );
    }
}
