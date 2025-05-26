package site.codemonster.comon.global.error.articles;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class SubjectDuplicatedException extends ComonException {
    public SubjectDuplicatedException() {
        super(ErrorCode.SUBJECT_DUPLICATED_ERROR);
    }
}
