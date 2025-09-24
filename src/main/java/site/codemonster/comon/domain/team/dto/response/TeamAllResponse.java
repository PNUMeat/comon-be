package site.codemonster.comon.domain.team.dto.response;

import site.codemonster.comon.domain.auth.dto.response.MemberProfileResponse;
import site.codemonster.comon.domain.team.entity.Team;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import site.codemonster.comon.global.util.s3.S3ImageUtil;

public record TeamAllResponse(Long teamId,
                              String teamName,
                              String teamExplain,
                              String imageUrl,
                              String topic,
                              int memberLimit,
                              int memberCount,
                              int streakDays,
                              LocalDate createdAt,
                              List<MemberProfileResponse> members)
{
    public TeamAllResponse(Team team) {
        this (
                team.getTeamId(),
                team.getTeamName(),
                team.getTeamExplain(),
                S3ImageUtil.convertObjectKeyToImageUrl(team.getTeamIconUrl()),
                team.getTeamTopic().getName(),
                team.getMaxParticipant(),
                team.getTeamMembers().size(),
                team.getStreakDays(),
                team.getCreatedDate().toLocalDate(),
                team.getTeamMembers().stream()
                        .map(tm -> new MemberProfileResponse(tm.getMember()))
                        .collect(Collectors.toList())
        );
    }
}


