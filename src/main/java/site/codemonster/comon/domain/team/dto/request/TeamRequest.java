package site.codemonster.comon.domain.team.dto.request;

import jakarta.validation.constraints.*;

import java.util.List;

public record TeamRequest(
        @NotBlank(message = "팀 이름은 필수 입력 항목입니다.")
        @Size(
                max = 10,
                message = "글자 수를 초과했습니다."
        )
        String teamName,

        @Size(
                max = 50,
                message = "글자 수를 초과했습니다."
        )
        String teamExplain,

        @NotBlank(message = "팀 주제는 필수 항목입니다.")
        String topic,

        @Min(value = 2, message = "2 ~ 50 사이의 숫자를 입력해주세요.")
        @Max(value = 50, message = "2 ~ 50 사이의 숫자를 입력해주세요.")
        int memberLimit,

        @NotBlank(message = "팀 비밀번호는 필수 항목입니다.")
        @Pattern(
                regexp = "^[0-9]{4}$",
                message = "숫자 4자리를 입력해주세요."
        )
        String password,

        List<String> teamMemberUuids,

        String teamIconUrl,

        Long teamRecruitId
) {
}
