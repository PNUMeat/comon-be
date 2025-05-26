package site.codemonster.comon.global.error.TeamApply;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class TeamApplyNotFoundException extends ComonException {
    public TeamApplyNotFoundException(){
        super(ErrorCode.TEAM_APPLY_NOT_FOUND_ERROR);
    }
}
