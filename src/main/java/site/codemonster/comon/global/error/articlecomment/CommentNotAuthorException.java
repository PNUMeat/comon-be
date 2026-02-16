package site.codemonster.comon.global.error.articlecomment;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class CommentNotAuthorException extends ComonException {
    public CommentNotAuthorException() {
        super(ErrorCode.COMMENT_NOT_AUTHOR_ERROR);
    }
}
