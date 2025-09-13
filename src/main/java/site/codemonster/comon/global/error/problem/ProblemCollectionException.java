package site.codemonster.comon.global.error.problem;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class ProblemCollectionException extends ComonException {
    public ProblemCollectionException() {
        super(ErrorCode.PROBLEM_COLLECTION_ERROR);
    }
}
