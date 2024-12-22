package PNUMEAT.Backend.global.error.articles;

import PNUMEAT.Backend.global.error.ComonException;
import PNUMEAT.Backend.global.error.ErrorCode;

public class TodaySubjectAlreadyCreatedException extends ComonException {
    public TodaySubjectAlreadyCreatedException() {
        super(ErrorCode.TODAY_SUBJECT_ALREADY_CREATED);
    }
}
