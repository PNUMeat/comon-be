package site.codemonster.comon.domain.article.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ArticleCreateRequest(
    @NotNull(message = "팀 아이디는 필수요소입니다.")
    Long teamId,
    @NotBlank(message = "게시글 제목은 필수요소입니다")
    String articleTitle,
    @NotBlank(message = "게시글 내용은 필수요소입니다")
    String articleBody
) {

}
