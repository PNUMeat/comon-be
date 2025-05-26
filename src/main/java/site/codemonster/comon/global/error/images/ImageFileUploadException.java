package site.codemonster.comon.global.error.images;


import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class ImageFileUploadException extends ComonException {
    public ImageFileUploadException() {
        super(ErrorCode.IMAGE_FILE_UPLOAD_ERROR);
    }

}
