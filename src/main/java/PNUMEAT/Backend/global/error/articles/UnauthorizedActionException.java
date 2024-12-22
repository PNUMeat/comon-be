package PNUMEAT.Backend.global.error.articles;


import PNUMEAT.Backend.global.error.ComonException;
import PNUMEAT.Backend.global.error.ErrorCode;

public class UnauthorizedActionException extends ComonException {
    public UnauthorizedActionException() {
        super(ErrorCode.UNAUTHORIZED_ACTION);
    }

}
