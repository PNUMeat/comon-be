package PNUMEAT.Backend.global.error.articles;

import PNUMEAT.Backend.global.error.ComonException;
import PNUMEAT.Backend.global.error.ErrorCode;

public class SubjectAlreadyCreatedException extends ComonException {
    public SubjectAlreadyCreatedException() {
        super(ErrorCode.SUBJECT_ALREADY_CREATED_ERROR);
    }
}
