package site.codemonster.comon.global.error.ArticleComment;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class CommentNotTeamMemberException extends ComonException {
    public CommentNotTeamMemberException() {
        super(ErrorCode.COMMENT_NOT_TEAM_MEMBER_ERROR);
    }
}
