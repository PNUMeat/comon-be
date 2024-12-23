package PNUMEAT.Backend.domain.article.dto.request;

public record TeamSubjectUpdateRequest(
        String articleCategory,
        String articleTitle,
        String articleBody
) {
}