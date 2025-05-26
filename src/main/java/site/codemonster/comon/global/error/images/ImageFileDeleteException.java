package site.codemonster.comon.global.error.images;


import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class ImageFileDeleteException extends ComonException {
    public ImageFileDeleteException() {
        super(ErrorCode.IMAGE_FILE_DELETE_ERROR);
    }

}
