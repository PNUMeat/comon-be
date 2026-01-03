package site.codemonster.comon.domain.article.dto.response;

import site.codemonster.comon.global.response.StreamEventType;

import static site.codemonster.comon.global.response.StreamEventType.*;

public record ArticleFeedbackStreamResponse(
        StreamEventType type,
        String content
) {

    public static ArticleFeedbackStreamResponse createStream(String content) {
        return new ArticleFeedbackStreamResponse(PROCESSING, content);
    }

    public static ArticleFeedbackStreamResponse complete() {
        return new ArticleFeedbackStreamResponse(COMPLETE, COMPLETE.getMessage());
    }
}
