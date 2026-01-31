package site.codemonster.comon.domain.team.dto.response;

import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.global.util.s3.S3ImageUtil;

public record TeamInfoResponse(Long teamId,
                               String teamName,
                               String teamExplain,
                               String topic,
                               int memberLimit,
                               String password,
                               String teamIconUrl)
{
    public TeamInfoResponse(Team team) {
        this(
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
