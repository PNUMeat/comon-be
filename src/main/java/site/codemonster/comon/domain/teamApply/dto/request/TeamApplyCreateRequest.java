package site.codemonster.comon.domain.teamApply.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TeamApplyCreateRequest(
        Long recruitmentId,
        @NotBlank(message = "팀 지원글 내용은 필수 요소입니다.")
        String teamApplyBody) {
}
