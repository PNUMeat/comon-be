package site.codemonster.comon.domain.article.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TeamSubjectRequest(
        @NotBlank(message = "게시글 카테고리는 필수 요소입니다.")
        String articleCategory,
        @NotBlank(message = "날짜 선택은 필수 요소입니다.")
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "날짜 형식은 yyyy-MM-dd이어야 합니다.")
        String selectedDate,
        @NotBlank(message = "게시글 제목은 필수 요소입니다.")
        String articleTitle,
        @NotBlank(message = "게시글 내용은 필수 요소입니다.")
        String articleBody
) {
}
