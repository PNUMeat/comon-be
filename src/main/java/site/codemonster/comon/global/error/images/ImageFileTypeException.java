package site.codemonster.comon.global.error.images;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class ImageFileTypeException extends ComonException {
	public ImageFileTypeException() {
		super(ErrorCode.IMAGE_FILE_TYPE_ERROR);
	}
}
