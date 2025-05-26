package site.codemonster.comon.global.error.Team;

import site.codemonster.comon.global.error.ComonException;

import static site.codemonster.comon.global.error.ErrorCode.TEAM_ALREADY_JOIN;

public class TeamAlreadyJoinException extends ComonException {
    public TeamAlreadyJoinException(){
        super(TEAM_ALREADY_JOIN);
    }
}
