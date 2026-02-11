package site.codemonster.comon.global.error.ArticleComment;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class CommentDeleteNotAuthorException extends ComonException {
    public CommentDeleteNotAuthorException() {
        super(ErrorCode.COMMENT_NOT_AUTHOR_DELETE_ERROR);
    }
}
