package site.codemonster.comon.domain.team.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TeamManagerUpdateRequest(
        @NotBlank(message = "회원 정보는 필수입니다.")
        String memberInfo
) {
}
