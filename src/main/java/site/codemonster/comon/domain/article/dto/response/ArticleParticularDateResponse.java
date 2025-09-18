package site.codemonster.comon.domain.article.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ArticleParticularDateResponse(
        Long articleId,
        String articleTitle,
        String articleBody,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdDate,
        String memberName,
        String memberImage,
        Boolean isAuthor
) {
}
