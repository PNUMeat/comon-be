package site.codemonster.comon.global.error.ArticleFeedback;


import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class AIFeedbackGenerationException extends ComonException {
    public AIFeedbackGenerationException() {
        super(ErrorCode.ARTICLE_FEEDBACK_GENERATION_ERROR);
    }
}
