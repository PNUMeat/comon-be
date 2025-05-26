package site.codemonster.comon.global.util.responseUtils;

import site.codemonster.comon.global.error.ErrorCode;
import site.codemonster.comon.global.error.dto.response.ApiResponse;
import site.codemonster.comon.global.error.response.ErrorResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResponseUtils {

    private final ObjectMapper objectMapper;

    public void generateErrorResponseInHttpServletResponse(ErrorCode errorCode, HttpServletResponse response) {
        ErrorResult errorResult = new ErrorResult(errorCode.getCustomStatusCode(), errorCode.getMessage());

        setErrorResponse(response, errorCode.getStatus(), errorResult);
    }

    private void setErrorResponse(HttpServletResponse response, HttpStatus status, ErrorResult errorResult) {
        try {
            String errorResponse = objectMapper.writeValueAsString(ApiResponse.errorResponse(errorResult));
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(errorResponse);
            response.setStatus(status.value());
        }
        catch (IOException e){
            log.error("IOException 발생");
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }
}
