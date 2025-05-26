package site.codemonster.comon.global.error.images;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class ImageFileNameException extends ComonException {
	public ImageFileNameException() {
		super(ErrorCode.IMAGE_FILE_NOT_FOUND);
	}
}
