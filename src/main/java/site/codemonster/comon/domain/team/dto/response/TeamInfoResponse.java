package site.codemonster.comon.domain.team.dto.response;

import site.codemonster.comon.domain.team.entity.Team;

public record TeamInfoResponse(Long teamId,
                               String teamName,
                               String teamExplain,
                               String topic,
                               int memberLimit,
                               String password,
                               String teamIconUrl) {
}
