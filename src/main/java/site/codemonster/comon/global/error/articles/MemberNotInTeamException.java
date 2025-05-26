package site.codemonster.comon.global.error.articles;


import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class MemberNotInTeamException extends ComonException {
    public MemberNotInTeamException() {
        super(ErrorCode.MEMBER_NOT_IN_TEAM);
    }

}
