package site.codemonster.comon.domain.team.dto.response;

import site.codemonster.comon.domain.team.entity.Team;

public record TeamCreateResponse (
        Long teamId
){
    public static TeamCreateResponse of(Team team){
        return new TeamCreateResponse(team.getTeamId());
    }
}
