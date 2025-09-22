package site.codemonster.comon.global.error.dto.response;

import site.codemonster.comon.global.error.response.ErrorResult;
import site.codemonster.comon.global.error.response.ErrorValidationResult;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Map;

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

    public static <T> ApiResponse<T> successResponse(T data, String message){
        return new ApiResponse<>(SUCCESS, HttpStatus.OK.value(), message, data);
    }

    public static <T> ApiResponse<T> successResponseWithData(T data){
        return new ApiResponse<>(SUCCESS, HttpStatus.OK.value(), null, data);
    }

    public static ApiResponse<Void> successResponseWithMessage(String message){
        return new ApiResponse<>(SUCCESS, HttpStatus.OK.value(), message, null);
    }

    public static <T> ApiResponse<T> createResponse(T data, String message){
        return new ApiResponse<>(SUCCESS, HttpStatus.CREATED.value(), message, data);
    }

    public static <T> ApiResponse<T> createResponseWithDate(T data){
        return new ApiResponse<>(SUCCESS, HttpStatus.CREATED.value(), null, data);
    }

    public static ApiResponse<Void> createResponseWithMessage(String message){
        return new ApiResponse<>(SUCCESS, HttpStatus.CREATED.value(), message, null);
    }

    public static ApiResponse<Map<String,String>> validationErrorResponse(ErrorValidationResult e){
        return new ApiResponse<>(FAIL, ErrorValidationResult.ERROR_STATUS_CODE, ErrorValidationResult.ERROR_MESSAGE, e.getValidation());
    }

    public static ApiResponse<Void> errorResponse(ErrorResult error){
        return new ApiResponse<>(ERROR, error.getStatusCode(), error.getMessage(), null);
    }
}
