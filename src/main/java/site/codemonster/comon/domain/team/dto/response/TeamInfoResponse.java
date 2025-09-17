package site.codemonster.comon.domain.team.dto.response;

import site.codemonster.comon.domain.team.entity.Team;

public record TeamInfoResponse(Long teamId,
                               String teamName,
                               String teamExplain,
                               String topic,
                               int memberLimit,
                               String password,
                               String teamIconUrl) {
    public static TeamInfoResponse of(Team team){
        return new TeamInfoResponse(
                team.getTeamId(),
                team.getTeamName(),
                team.getTeamExplain(),
                team.getTeamTopic().getName(),
                team.getMaxParticipant(),
                team.getTeamPassword(),
                team.getTeamIconUrl()
        );
    }
}
