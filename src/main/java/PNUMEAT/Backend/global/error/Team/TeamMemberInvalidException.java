package PNUMEAT.Backend.global.error.Team;

import PNUMEAT.Backend.global.error.ComonException;

import static PNUMEAT.Backend.global.error.ErrorCode.TEAM_MEMBER_INVALID_ERROR;

public class TeamMemberInvalidException extends ComonException {
    public TeamMemberInvalidException(){
        super(TEAM_MEMBER_INVALID_ERROR);
    }
}
