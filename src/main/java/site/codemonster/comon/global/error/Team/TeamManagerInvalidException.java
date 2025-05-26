package site.codemonster.comon.global.error.Team;

import site.codemonster.comon.global.error.ComonException;

import static site.codemonster.comon.global.error.ErrorCode.TEAM_MANAGER_INVALID_ERROR;

public class TeamManagerInvalidException extends ComonException {
    public TeamManagerInvalidException(){
        super(TEAM_MANAGER_INVALID_ERROR);
    }
}
