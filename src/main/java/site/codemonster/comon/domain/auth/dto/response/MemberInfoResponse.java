package site.codemonster.comon.domain.auth.dto.response;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.team.dto.response.TeamAbstractResponse;

import java.util.List;
import site.codemonster.comon.global.util.s3.S3ImageUtil;

public record MemberInfoResponse(
        String memberName,
        String memberImageUrl,
        List<TeamAbstractResponse> teamAbstractResponses
) {
    public MemberInfoResponse(Member member, List<TeamAbstractResponse> teamAbstractResponses) {
        this(
                member.getMemberName(),
                S3ImageUtil.convertObjectKeyToImageUrl(member.getImageUrl()),
                teamAbstractResponses
        );
    }
}
