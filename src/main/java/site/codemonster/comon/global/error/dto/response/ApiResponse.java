package site.codemonster.comon.global.error.dto.response;

import site.codemonster.comon.global.error.response.ErrorResult;
import site.codemonster.comon.global.error.response.ErrorValidationResult;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@Builder
@RequiredArgsConstructor
public class ApiResponse<T> {

    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";
    public static final String ERROR = "error";
    private final String status;
    private final int code;
    private final String message;
    private final T data;

    public static <T> ApiResponse<?> successResponse(T data, String message){
        return new ApiResponse<>(SUCCESS, HttpStatus.OK.value(), message, data);
    }

    public static <T> ApiResponse<?> successResponseWithData(T data){
        return new ApiResponse<>(SUCCESS, HttpStatus.OK.value(), null, data);
    }

    public static <T> ApiResponse<?> successResponseWithMessage(String message){
        return new ApiResponse<>(SUCCESS, HttpStatus.OK.value(), message, null);
    }

    public static <T> ApiResponse<?> createResponse(T data, String message){
        return new ApiResponse<>(SUCCESS, HttpStatus.CREATED.value(), message, data);
    }

    public static <T> ApiResponse<?> createResponseWithDate(T data){
        return new ApiResponse<>(SUCCESS, HttpStatus.CREATED.value(), null, data);
    }

    public static <T> ApiResponse<?> createResponseWithMessage(String message){
        return new ApiResponse<>(SUCCESS, HttpStatus.CREATED.value(), message, null);
    }

    public static ApiResponse<?> validationErrorResponse(ErrorValidationResult e){
        return new ApiResponse<>(FAIL, ErrorValidationResult.ERROR_STATUS_CODE, ErrorValidationResult.ERROR_MESSAGE, e.getValidation());
    }

    public static ApiResponse<?> errorResponse(ErrorResult error){
        return new ApiResponse<>(ERROR, error.getStatusCode(), error.getMessage(), null);
    }
}
