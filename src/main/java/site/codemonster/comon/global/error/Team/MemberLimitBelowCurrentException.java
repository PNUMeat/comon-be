package site.codemonster.comon.global.error.Team;


import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class MemberLimitBelowCurrentException extends ComonException {
    public MemberLimitBelowCurrentException(){
        super(ErrorCode.MEMBER_LIMIT_BELOW_CURRENT_ERROR);
    }
}
