package site.codemonster.comon.domain.teamRecruit.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TeamRecruitInviteRequest(
        @NotNull(message = "팀이 존재하지 않습니다.")
        Long teamId,

        @NotNull(message = "모집글이 존재하지 않습니다.")
        Long recruitId,

        @NotNull(message = "지원한 사람이 없습니다.")
        List<String> memberUuids
) {
}
