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
}


