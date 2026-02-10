package site.codemonster.comon.domain.article.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArticleCommentUpdateRequest(
        @NotBlank(message = "댓글 내용은 필수요소입니다.")
        @Size(max = 300, message = "댓글 길이는 300자 이하입니다.")
        String description
) {}
