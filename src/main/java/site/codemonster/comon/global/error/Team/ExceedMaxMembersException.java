package site.codemonster.comon.global.error.Team;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class ExceedMaxMembersException extends ComonException {
    public ExceedMaxMembersException(){
        super(ErrorCode.EXCEED_MAX_MEMBERS);
    }
}