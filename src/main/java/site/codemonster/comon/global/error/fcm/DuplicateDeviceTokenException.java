package site.codemonster.comon.global.error.fcm;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class DuplicateDeviceTokenException extends ComonException {
    public DuplicateDeviceTokenException() {
        super(ErrorCode.DUPLICATE_DEVICE_TOKEN);
    }
}
