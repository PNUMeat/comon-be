package site.codemonster.comon.domain.teamApply.dto.response;

import site.codemonster.comon.domain.teamApply.entity.TeamApply;

import java.util.List;
import java.util.stream.Collectors;

public record TeamApplyMemberResponse(
        List<String> teamMemberUuids
) {
    public static List<String> of(List<TeamApply> teamApplies){
        return teamApplies.stream()
                .map(teamApply -> teamApply.getMember().getUuid())
                .collect(Collectors.toList());
    }
}
