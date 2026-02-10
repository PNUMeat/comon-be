package site.codemonster.comon.global.error.ArticleComment;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class CommentReadNotTeamMemberException extends ComonException {
    public CommentReadNotTeamMemberException() {
        super(ErrorCode.COMMENT_NOT_TEAM_MEMBER_READ_ERROR);
    }
}
