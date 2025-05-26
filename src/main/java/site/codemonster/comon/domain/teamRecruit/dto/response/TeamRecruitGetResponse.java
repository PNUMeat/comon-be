package site.codemonster.comon.domain.teamRecruit.dto.response;

import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record TeamRecruitGetResponse (
        Long teamRecruitId,
        String teamRecruitTitle,
        String teamRecruitBody,
        String memberNickName,
        Boolean isRecruiting,
        Long teamId,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate createdAt
)
{
    public static TeamRecruitGetResponse of(TeamRecruit teamRecruit){
        Long teamId = null;
        if(teamRecruit.getTeam() != null){
            teamId = teamRecruit.getTeam().getTeamId();
        }

        return new TeamRecruitGetResponse(
                teamRecruit.getTeamRecruitId(),
                teamRecruit.getTeamRecruitTitle(),
                teamRecruit.getTeamRecruitBody(),
                teamRecruit.getMember().getMemberName(),
                teamRecruit.isRecruiting(),
                teamId,
                teamRecruit.getCreatedDate().toLocalDate()
        );
    }
}
