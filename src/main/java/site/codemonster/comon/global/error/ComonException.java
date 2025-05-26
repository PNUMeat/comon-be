package site.codemonster.comon.global.error;

public class ComonException extends RuntimeException{
    private final ErrorCode errorCode;

    public ComonException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public int getStatusCode(){
        return errorCode.getStatus().value();
    }
}
