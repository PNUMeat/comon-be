package site.codemonster.comon.domain.article.dto.request;

public record TeamSubjectUpdateRequest(
        String articleCategory,
        String articleTitle,
        String articleBody
) {
}