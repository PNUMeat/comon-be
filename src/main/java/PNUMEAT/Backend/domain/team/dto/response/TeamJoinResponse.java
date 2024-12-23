package PNUMEAT.Backend.domain.team.dto.response;

import PNUMEAT.Backend.domain.teamMember.entity.TeamMember;

public record TeamJoinResponse(Long teamId) {
    public static TeamJoinResponse of(TeamMember teamMember){
        return new TeamJoinResponse(
                teamMember.getTeam().getTeamId()
        );
    }
}
