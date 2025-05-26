package site.codemonster.comon.global.error.images;


import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class S3Exception extends ComonException {
    public S3Exception() {
        super(ErrorCode.S3_NETWORK_ERROR);
    }

}
