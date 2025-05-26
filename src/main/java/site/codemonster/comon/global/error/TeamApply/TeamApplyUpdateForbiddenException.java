package site.codemonster.comon.global.error.TeamApply;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class TeamApplyUpdateForbiddenException extends ComonException {
    public TeamApplyUpdateForbiddenException(){
        super(ErrorCode.TEAM_APPLY_UPDATE_FORBIDDEN_ERROR);
    }
}
