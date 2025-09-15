package site.codemonster.comon.global.error.problem;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class ProblemBatchRegisterException extends ComonException {
    public ProblemBatchRegisterException(ErrorCode errorCode) {
        super(errorCode);
    }
}
