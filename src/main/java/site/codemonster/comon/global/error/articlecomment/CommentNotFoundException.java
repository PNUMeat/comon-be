package site.codemonster.comon.global.error.articlecomment;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class CommentNotFoundException extends ComonException {
    public CommentNotFoundException() {
        super(ErrorCode.COMMENT_NOT_FOUND_ERROR);
    }
}
