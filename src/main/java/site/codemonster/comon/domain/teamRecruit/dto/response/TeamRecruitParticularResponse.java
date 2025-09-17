package site.codemonster.comon.domain.teamRecruit.dto.response;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.teamApply.dto.response.TeamApplyGetResponse;
import site.codemonster.comon.domain.teamApply.entity.TeamApply;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record TeamRecruitParticularResponse(
        Long teamRecruitId,
        String teamRecruitTitle,
        String teamRecruitBody,
        String chatUrl,
        Boolean isRecruiting,
        String memberNickName,
        Boolean isAuthor,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDateTime createdAt,
        Long teamId,
        List<TeamApplyGetResponse> teamApplyResponses,
        List<String> teamMemberUuids
) {
    public static TeamRecruitParticularResponse from(TeamRecruit teamRecruit, List<TeamApply> teamApplies, Member member, List<String> teamMemberUuids){
        List<TeamApplyGetResponse> applies;
        if(teamRecruit.isAuthor(member)){
            applies = teamApplies.stream()
                    .map((TeamApply teamApply) -> TeamApplyGetResponse.from(teamApply, member))
                    .collect(Collectors.toList());
        }else{
            applies = new ArrayList<>();
            for (TeamApply teamApply : teamApplies) {
                if(teamApply.isAuthor(member)){
                    applies.add(new TeamApplyGetResponse(teamApply.getTeamApplyId(), teamApply.getTeamApplyBody(), teamApply.getMember().getMemberName(), teamApply.isAuthor(member)));
                }else{
                    applies.add(new TeamApplyGetResponse(teamApply.getTeamApplyId(), null, teamApply.getMember().getMemberName(), teamApply.isAuthor(member)));
                }
            }
        }

        Long teamId = null;
        if(teamRecruit.getTeam() != null){
            teamId = teamRecruit.getTeam().getTeamId();
        }

        return new TeamRecruitParticularResponse(
                teamRecruit.getTeamRecruitId(),
                teamRecruit.getTeamRecruitTitle(),
                teamRecruit.getTeamRecruitBody(),
                teamRecruit.getChatUrl(),
                teamRecruit.isRecruiting(),
                teamRecruit.getMember().getMemberName(),
                teamRecruit.isAuthor(member),
                teamRecruit.getCreatedDate(),
                teamId,
                applies,
                teamMemberUuids
        );
    }
}
