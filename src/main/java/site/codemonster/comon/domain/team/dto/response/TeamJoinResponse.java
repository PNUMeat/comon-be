package site.codemonster.comon.domain.team.dto.response;

import site.codemonster.comon.domain.teamMember.entity.TeamMember;

public record TeamJoinResponse(Long teamId) {
    public static TeamJoinResponse of(TeamMember teamMember){
        return new TeamJoinResponse(
                teamMember.getTeam().getTeamId()
        );
    }
}
