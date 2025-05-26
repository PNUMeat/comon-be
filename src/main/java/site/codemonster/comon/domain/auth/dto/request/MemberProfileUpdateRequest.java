package site.codemonster.comon.domain.auth.dto.request;


public record MemberProfileUpdateRequest(
        String memberName,
        String memberExplain,
        String imageUrl
) {
}
