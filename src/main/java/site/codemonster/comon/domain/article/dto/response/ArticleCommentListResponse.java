package site.codemonster.comon.domain.article.dto.response;

import java.util.List;

public record ArticleCommentListResponse(
        List<ArticleCommentResponse> comments
) {}
