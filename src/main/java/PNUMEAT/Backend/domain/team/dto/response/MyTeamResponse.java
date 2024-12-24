package PNUMEAT.Backend.domain.team.dto.response;

import PNUMEAT.Backend.domain.team.entity.Team;

import java.time.LocalDate;

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
                              LocalDate createdAt) {

    public static MyTeamResponse of(Team team){
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
                team.getCreatedDate().toLocalDate()
        );
    }
}
