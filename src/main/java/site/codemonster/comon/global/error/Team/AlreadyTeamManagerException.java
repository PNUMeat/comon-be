package site.codemonster.comon.global.error.Team;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class AlreadyTeamManagerException extends ComonException {
    public AlreadyTeamManagerException(){
        super(ErrorCode.ALREADY_TEAM_MANAGER);
    }
}