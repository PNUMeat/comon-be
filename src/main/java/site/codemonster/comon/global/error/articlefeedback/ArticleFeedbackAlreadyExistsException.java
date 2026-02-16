package site.codemonster.comon.global.error.articlefeedback;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class ArticleFeedbackAlreadyExistsException extends ComonException {
    public ArticleFeedbackAlreadyExistsException() {
        super(ErrorCode.ARTICLE_FEEDBACK_ALREADY_EXISTS_ERROR);
    }
}
