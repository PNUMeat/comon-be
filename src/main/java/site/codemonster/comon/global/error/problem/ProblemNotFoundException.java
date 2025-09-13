package site.codemonster.comon.global.error.problem;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class ProblemNotFoundException extends ComonException {
    public ProblemNotFoundException() {
        super(ErrorCode.PROBLEM_NOT_FOUND_ERROR);
    }
}
