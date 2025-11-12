package site.codemonster.comon.global.error.ArticleFeedback;


import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class ArticleFeedbackNotFoundException extends ComonException {
    public ArticleFeedbackNotFoundException() {
        super(ErrorCode.ARTICLE_FEEDBACK_NOT_FOUND_ERROR);
    }
}
