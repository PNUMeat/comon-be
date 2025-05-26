package site.codemonster.comon.global.error.Team;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class TeamNotFoundException extends ComonException {
    public TeamNotFoundException(){
        super(ErrorCode.TEAM_NOT_FOUND_ERROR);
    }
}
