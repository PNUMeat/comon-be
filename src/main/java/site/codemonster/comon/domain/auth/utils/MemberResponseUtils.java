package site.codemonster.comon.domain.auth.utils;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import site.codemonster.comon.domain.auth.dto.response.MemberInfoResponse;
import site.codemonster.comon.domain.auth.dto.response.MemberProfileResponse;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.team.dto.response.TeamAbstractResponse;
import site.codemonster.comon.global.util.convertUtils.ImageFieldConvertUtils;

@Component
@RequiredArgsConstructor
public class MemberResponseUtils {
    private final ImageFieldConvertUtils imageFieldConvertUtils;

    public MemberProfileResponse getMemberProfileResponse(Member member) {
        return new MemberProfileResponse(
                member.getMemberName(),
                imageFieldConvertUtils.convertObjectKeyToImageUrl(member.getImageUrl()),
                member.getDescription(),
                member.getUuid()
        );
    }

    public MemberInfoResponse getMemberInfoResponse(Member member, List<TeamAbstractResponse> teamAbstractResponse) {
        return new MemberInfoResponse(
                member.getMemberName(),
                imageFieldConvertUtils.convertObjectKeyToImageUrl(member.getImageUrl()),
                teamAbstractResponse
        );
    }
}
