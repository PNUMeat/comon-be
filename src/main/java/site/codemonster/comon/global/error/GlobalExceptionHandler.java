package site.codemonster.comon.global.error;

import site.codemonster.comon.global.error.dto.response.ApiResponse;
import site.codemonster.comon.global.error.response.ErrorResult;
import site.codemonster.comon.global.error.response.ErrorValidationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.format.DateTimeParseException;
import java.util.Map;

import static site.codemonster.comon.global.error.ErrorCode.DATETIME_PARSE_ERROR;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ResponseBody
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String,String>>> invalidRequestHandler(MethodArgumentNotValidException e) {
    ErrorValidationResult errorValidationResult = new ErrorValidationResult();

    for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
      errorValidationResult.addValidation(fieldError.getField(), fieldError.getDefaultMessage());
    }

    // 가장 최근의 message를 대표 errorMessage로 설정
    if (!e.getBindingResult().getFieldErrors().isEmpty()) {
      errorValidationResult.setErrorMessage(e.getBindingResult().getFieldErrors().get(0).getDefaultMessage());
    }

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(ApiResponse.validationErrorResponse(errorValidationResult));
  }

  @ResponseBody
  @ExceptionHandler(DateTimeParseException.class)
  public ResponseEntity<ApiResponse<?>> dateTimeParseExceptionHandler(DateTimeParseException e) {
    ErrorResult errorResult = new ErrorResult(DATETIME_PARSE_ERROR.getStatus().value(), DATETIME_PARSE_ERROR.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ApiResponse.errorResponse(errorResult));
  }

  @ExceptionHandler(ComonException.class)
  public ResponseEntity<ApiResponse<Void>> comonExceptionHandler(ComonException e) {
    ErrorResult errorResult = new ErrorResult(e.getStatusCode(), e.getMessage());

    return ResponseEntity.status(HttpStatus.valueOf(e.getStatusCode()))
            .contentType(MediaType.APPLICATION_JSON)
            .body(ApiResponse.errorResponse(errorResult));
  }
}
