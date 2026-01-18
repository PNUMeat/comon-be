package site.codemonster.comon.global.error.response;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ErrorValidationResult {
    public static int ERROR_STATUS_CODE = 400;
    private String errorMessage = "유효성 검사를 통과하지 못했습니다.";
    private final Map<String, String> validation = new HashMap<>();

    public void addValidation(String fieldName, String errorMessage) {
        this.validation.put(fieldName, errorMessage);
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
