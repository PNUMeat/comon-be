package site.codemonster.comon.domain.teamRecruit.dto.request;

import jakarta.validation.constraints.Size;

public record TeamRecruitUpdateRequest (
        @Size(
                max = 50,
                message = "글자 수를 초과했습니다."
        )
        String teamRecruitTitle,
        String teamRecruitBody,
        String chatUrl
) {
}