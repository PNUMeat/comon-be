package site.codemonster.comon.domain.article.controller;

import lombok.Getter;

@Getter
public enum ArticleFeedbackResponseEnum {
    ARTICLE_FEEDBACK_GENERATE_SUCCESS("AI 피드백이 생성되었습니다.",200),
    ARTICLE_FEEDBACK_REGENERATE_SUCCESS("AI 피드백이 재생성되었습니다.",200),
    GET_ARTICLE_FEEDBACK("해당 게시물의 AI 피드백을 조회했습니다.", 200)
    ;

    private final String message;
    private final int statusCode;

    ArticleFeedbackResponseEnum(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }
}
