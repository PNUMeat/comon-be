package site.codemonster.comon.domain.team.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TeamInfoEditRequest(
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

        String topic,

        @Min(value = 2, message = "2 ~ 50 사이의 숫자를 입력해주세요.")
        @Max(value = 50, message = "2 ~ 50 사이의 숫자를 입력해주세요.")
        Integer memberLimit,

        @Pattern(
                regexp = "^([0-9]{4})?$",
                message = "숫자 4자리를 입력해주세요."
        )
        String password,

        String teamIconUrl
) {
}
