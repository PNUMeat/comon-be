package site.codemonster.comon.domain.auth.dto.request;

import jakarta.validation.constraints.Size;

public record MemberProfileUpdateRequest(
        String memberName,
        @Size(max = 40, message = "소개는 최대 40자까지 입력할 수 있습니다.")
        String memberExplain,
        String imageUrl
) {
}
