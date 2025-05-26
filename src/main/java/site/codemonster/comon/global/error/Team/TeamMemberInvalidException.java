package site.codemonster.comon.global.error.Team;

import site.codemonster.comon.global.error.ComonException;

import static site.codemonster.comon.global.error.ErrorCode.TEAM_MEMBER_INVALID_ERROR;

public class TeamMemberInvalidException extends ComonException {
    public TeamMemberInvalidException(){
        super(TEAM_MEMBER_INVALID_ERROR);
    }
}
