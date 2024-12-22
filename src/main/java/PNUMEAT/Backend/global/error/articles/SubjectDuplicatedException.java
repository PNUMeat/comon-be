package PNUMEAT.Backend.global.error.articles;

import PNUMEAT.Backend.global.error.ComonException;
import PNUMEAT.Backend.global.error.ErrorCode;

public class SubjectDuplicatedException extends ComonException {
    public SubjectDuplicatedException() {
        super(ErrorCode.SUBJECT_DUPLICATED_ERROR);
    }
}
