package site.codemonster.comon.domain.auth.dto.request;

import jakarta.validation.constraints.NotNull;

public record MemberProfileCreateRequest(
        @NotNull
        String memberName,
        @NotNull
        String memberExplain,
        String imageUrl
) {
}
