package site.codemonster.comon.domain.teamRecruit.dto.response;

import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;

public record TeamRecruitCreateResponse(
        Long teamRecruitId
) {
    public static TeamRecruitCreateResponse of(TeamRecruit teamRecruit){
        return new TeamRecruitCreateResponse(teamRecruit.getTeamRecruitId());
    }
}
