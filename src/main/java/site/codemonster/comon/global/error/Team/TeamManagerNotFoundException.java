package site.codemonster.comon.global.error.Team;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class TeamManagerNotFoundException extends ComonException {
    public TeamManagerNotFoundException() {
        super(ErrorCode.TEAM_MANAGER_NOT_FOUND);
    }
}
