package site.codemonster.comon.domain.teamMember.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import site.codemonster.comon.domain.team.dto.response.TeamMemberResponse;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.global.util.convertUtils.ImageFieldConvertUtils;

@Component
@RequiredArgsConstructor
public class TeamMemberResponseUtils {
    private final ImageFieldConvertUtils imageFieldConvertUtils;

    public TeamMemberResponse getTeamMemberResponse(TeamMember teamMember){
        return new TeamMemberResponse(teamMember.getMember().getUuid(),
                teamMember.getMember().getMemberName(),
                teamMember.getMember().getDescription(),
                imageFieldConvertUtils.convertObjectKeyToImageUrl(teamMember.getMember().getImageUrl()),
                teamMember.getCreatedDate(),
                teamMember.getIsTeamManager());
    }
}
