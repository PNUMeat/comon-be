package site.codemonster.comon.domain.team.dto.response;

import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record MyTeamMyPageResponse(
        Long teamId,
        String teamName,
        Boolean teamManager,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime registerDate
) {
    public static MyTeamMyPageResponse of(TeamMember teamMember){
        return new MyTeamMyPageResponse(
                teamMember.getTeam().getTeamId(),
                teamMember.getTeam().getTeamName(),
                teamMember.getIsTeamManager(),
                teamMember.getCreatedDate()
        );
    }
}
