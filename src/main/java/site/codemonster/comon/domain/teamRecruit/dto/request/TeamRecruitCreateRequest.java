package site.codemonster.comon.domain.teamRecruit.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TeamRecruitCreateRequest(
        Long teamId,
        @NotBlank(message = "팀 모집글 제목은 필수요소입니다")
        String teamRecruitTitle,
        @NotBlank(message = "팀 모집글 내용은 필수요소입니다")
        String teamRecruitBody,
        @NotBlank(message = "팀 모집글 내용은 필수요소입니다")
        String chatUrl
) {
}