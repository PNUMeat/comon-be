package site.codemonster.comon.domain.article.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import site.codemonster.comon.global.validation.annotation.NotEmptyHtml;

public record ArticleCreateRequest(
    @NotNull(message = "팀 아이디는 필수요소입니다.")
    Long teamId,

    @NotBlank(message = "게시글 제목은 필수요소입니다")
    String articleTitle,

    @NotEmptyHtml(message = "게시글 내용은 필수요소입니다")
    String articleBody,

    @NotNull(message = "공개 여부는 필수요소입니다.")
    Boolean isVisible
) {

}
