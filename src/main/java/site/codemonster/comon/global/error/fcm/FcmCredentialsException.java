package site.codemonster.comon.global.error.fcm;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

import static site.codemonster.comon.global.error.ErrorCode.FIREBASE_CONNECT_ERROR;

public class FcmCredentialsException extends ComonException {

    public FcmCredentialsException() {
        super(FIREBASE_CONNECT_ERROR);
    }
}
