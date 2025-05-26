package site.codemonster.comon.global.error.articles;


import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class UnauthorizedActionException extends ComonException {
    public UnauthorizedActionException() {
        super(ErrorCode.UNAUTHORIZED_ACTION);
    }

}
