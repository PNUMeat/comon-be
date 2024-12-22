package PNUMEAT.Backend.domain.article.dto.request;

import PNUMEAT.Backend.domain.article.enums.ArticleCategory;
import PNUMEAT.Backend.global.validation.annotation.NotNullOrBlank;
import jakarta.validation.constraints.NotBlank;

public record TeamSubjectRequest(
        @NotNullOrBlank(message = "게시글 카테고리는 필수 요소입니다.")
        ArticleCategory articleCategory,
        @NotBlank(message = "게시글 제목은 필수 요소입니다.")
        String articleTitle,
        @NotBlank(message = "게시글 내용은 필수 요소854입니다.")
        String articleBody
) {

}
