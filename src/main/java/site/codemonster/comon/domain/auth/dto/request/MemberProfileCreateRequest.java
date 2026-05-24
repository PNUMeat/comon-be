package site.codemonster.comon.domain.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemberProfileCreateRequest(
        @NotNull
        String memberName,
        @NotNull
        @Size(max = 40, message = "소개는 최대 40자까지 입력할 수 있습니다.")
        String memberExplain,
        String imageUrl
) {
}
