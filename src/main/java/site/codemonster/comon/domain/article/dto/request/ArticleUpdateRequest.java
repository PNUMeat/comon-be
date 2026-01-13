package site.codemonster.comon.domain.article.dto.request;

import jakarta.validation.constraints.NotBlank;
import site.codemonster.comon.global.validation.annotation.NotEmptyHtml;

public record ArticleUpdateRequest(
        @NotBlank(message = "게시글 제목은 필수요소입니다")
        String articleTitle,
        @NotEmptyHtml(message = "게시글 내용은 필수요소입니다")
        String articleBody) {
}
