package site.codemonster.comon.domain.teamApply.dto.response;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.teamApply.entity.TeamApply;

public record TeamApplyGetResponse(
        Long teamApplyId,
        String teamApplyBody,
        String memberName,
        Boolean isMyApply
)
{
    public static TeamApplyGetResponse from(TeamApply teamApply, Member member){
        return new TeamApplyGetResponse(
                teamApply.getTeamApplyId(),
                teamApply.getTeamApplyBody(),
                teamApply.getMember().getMemberName(),
                teamApply.isAuthor(member)
        );
    }
}
