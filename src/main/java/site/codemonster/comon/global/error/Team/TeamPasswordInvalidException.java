package site.codemonster.comon.global.error.Team;

import site.codemonster.comon.global.error.ComonException;

import static site.codemonster.comon.global.error.ErrorCode.TEAM_PASSWORD_INVALID;

public class TeamPasswordInvalidException extends ComonException {
    public TeamPasswordInvalidException(){
        super(TEAM_PASSWORD_INVALID);
    }
}
