package site.codemonster.comon.global.error.problem;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class ProblemInvalidInputException extends ComonException {
    public ProblemInvalidInputException() {
        super(ErrorCode.PROBLEM_INVALID_INPUT_ERROR);
    }
}
