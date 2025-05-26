package site.codemonster.comon.domain.team.dto.response;

import site.codemonster.comon.domain.team.entity.Team;

public record TeamAbstractResponse(
        Long teamId,
        String teamName,
        String teamImageUrl
) {
    public static TeamAbstractResponse of(Team team){
        return new TeamAbstractResponse(team.getTeamId(), team.getTeamName(), team.getTeamIconUrl());
    }
}
