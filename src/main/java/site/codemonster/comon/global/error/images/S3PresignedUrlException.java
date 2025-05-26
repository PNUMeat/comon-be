package site.codemonster.comon.global.error.images;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class S3PresignedUrlException extends ComonException {
	public S3PresignedUrlException() {
		super(ErrorCode.S3_PRESIGNED_URL_ERROR);
	}
}
