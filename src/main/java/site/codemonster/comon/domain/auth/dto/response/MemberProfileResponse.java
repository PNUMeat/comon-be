package site.codemonster.comon.domain.auth.dto.response;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.global.util.s3.S3ImageUtil;

public record MemberProfileResponse(
        String memberName,
        String imageUrl,
        String memberExplain,
        String uuid
) {
    public MemberProfileResponse(Member member) {
        this(
                member.getMemberName(),
                member.getImageUrl(),
                member.getDescription(),
                member.getUuid()
        );
    }
}
