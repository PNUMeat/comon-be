package site.codemonster.comon.domain.auth.dto.response;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.team.dto.response.TeamAbstractResponse;

import java.util.List;

public record MemberInfoResponse(
        String memberName,
        String memberImageUrl,
        List<TeamAbstractResponse> teamAbstractResponses
)
{
    public static MemberInfoResponse from(Member member, List<TeamAbstractResponse> teamAbstractResponses){
        return new MemberInfoResponse(member.getMemberName(), member.getImageUrl(), teamAbstractResponses);
    }
}
