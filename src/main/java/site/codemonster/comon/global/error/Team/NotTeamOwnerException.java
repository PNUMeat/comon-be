package site.codemonster.comon.global.error.Team;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class NotTeamOwnerException extends ComonException {
    public NotTeamOwnerException(){
        super(ErrorCode.NOT_TEAM_OWNER);
    }
}
