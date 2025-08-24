package site.codemonster.comon.domain.team.dto.response;

import lombok.Builder;
import lombok.Data;
import site.codemonster.comon.domain.team.entity.Team;

@Data
@Builder
public class TeamRecommendationResponse {
    private Long teamId;
    private String teamName;
    private String teamIconUrl;
    private Integer memberCount;

    public static TeamRecommendationResponse from(Team team) {
        return TeamRecommendationResponse.builder()
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .teamIconUrl(team.getTeamIconUrl())
                .memberCount(team.getTeamMembers() != null ? team.getTeamMembers().size() : 0)
                .build();
    }
}
