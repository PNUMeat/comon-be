package site.codemonster.comon.domain.team.dto.response;

import site.codemonster.comon.domain.team.entity.Team;

import java.time.LocalDate;
import site.codemonster.comon.global.util.s3.S3ImageUtil;

public record MyTeamResponse(Long teamId,
                              String teamName,
                              String teamExplain,
                              String imageUrl,
                              String topic,
                              int memberLimit,
                              int memberCount,
                              int streakDays,
                              int successMemberCount,
                              String teamAnnouncement,
                              Long teamRecruitId,
                              LocalDate createdAt) {

    public static MyTeamResponse of(Team team){
        Long teamRecruitId = null;
        if (team.getTeamRecruit() != null){
            teamRecruitId = team.getTeamRecruit().getTeamRecruitId();
        }

        return new MyTeamResponse(
                team.getTeamId(),
                team.getTeamName(),
                team.getTeamExplain(),
                team.getTeamIconUrl(),
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
