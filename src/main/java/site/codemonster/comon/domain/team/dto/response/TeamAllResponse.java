package site.codemonster.comon.domain.team.dto.response;

import site.codemonster.comon.domain.auth.dto.response.MemberProfileResponse;
import site.codemonster.comon.domain.team.entity.Team;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public record TeamAllResponse(Long teamId,
                              String teamName,
                              String teamExplain,
                              String imageUrl,
                              String topic,
                              int memberLimit,
                              int memberCount,
                              int streakDays,
                              LocalDate createdAt,
                              List<MemberProfileResponse> members) {

    public static TeamAllResponse of(Team team){
        return new TeamAllResponse(
                team.getTeamId(),
                team.getTeamName(),
                team.getTeamExplain(),
                team.getTeamIconUrl(),
                team.getTeamTopic().getName(),
                team.getMaxParticipant(),
                team.getTeamMembers().size(),
                team.getStreakDays(),
                team.getCreatedDate().toLocalDate(),
                team.getTeamMembers().stream()
                        .map(tm -> MemberProfileResponse.of(tm.getMember()))
                        .collect(Collectors.toList())
        );
    }
}


