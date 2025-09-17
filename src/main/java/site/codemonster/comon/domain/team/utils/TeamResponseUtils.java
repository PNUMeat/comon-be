package site.codemonster.comon.domain.team.utils;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import site.codemonster.comon.domain.auth.utils.MemberResponseUtils;
import site.codemonster.comon.domain.team.dto.response.MyTeamResponse;
import site.codemonster.comon.domain.team.dto.response.TeamAllResponse;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.global.util.convertUtils.ImageFieldConvertUtils;

@Component
@RequiredArgsConstructor
public class TeamResponseUtils {
    private final ImageFieldConvertUtils imageFieldConvertUtils;
    private final MemberResponseUtils memberResponseUtils;

    public TeamAllResponse getTeamAllResponse(Team team){
        return new TeamAllResponse(
                team.getTeamId(),
                team.getTeamName(),
                team.getTeamExplain(),
                imageFieldConvertUtils.convertObjectKeyToImageUrl(team.getTeamIconUrl()),
                team.getTeamTopic().getName(),
                team.getMaxParticipant(),
                team.getTeamMembers().size(),
                team.getStreakDays(),
                team.getCreatedDate().toLocalDate(),
                team.getTeamMembers().stream()
                        .map(tm -> memberResponseUtils.getMemberProfileResponse((tm.getMember())))
                        .collect(Collectors.toList())
        );
    }

    public MyTeamResponse getMyTeamResponse(Team team) {
        Long teamRecruitId = null;
        if(team.getTeamRecruit() != null){
            teamRecruitId = team.getTeamRecruit().getTeamRecruitId();
        }

        return new MyTeamResponse(
                team.getTeamId(),
                team.getTeamName(),
                team.getTeamExplain(),
                imageFieldConvertUtils.convertObjectKeyToImageUrl(team.getTeamIconUrl()),
                team.getTeamTopic().getName(),
                team.getMaxParticipant(),
                team.getTeamMembers().size(),
                team.getStreakDays(),
                0, // 게시물 엔티티 없는 관계로 0으로 설정
                team.getTeamAnnouncement(),
                teamRecruitId,
                team.getCreatedDate().toLocalDate()
        );
    }
}
