package site.codemonster.comon.global.error.TeamApply;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class TeamApplyDeleteForbiddenException extends ComonException {
    public TeamApplyDeleteForbiddenException(){
        super(ErrorCode.TEAM_APPLY_DELETE_FORBIDDEN_ERROR);
    }
}
