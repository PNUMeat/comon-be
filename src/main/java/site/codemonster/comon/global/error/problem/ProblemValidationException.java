package site.codemonster.comon.global.error.problem;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class ProblemValidationException extends ComonException {
    public ProblemValidationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
