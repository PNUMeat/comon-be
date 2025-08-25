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

    public static String createRecommendationResponseMessage(int totalRecommended, StringBuilder failureMessageBuilder) {
        if (totalRecommended > 0) {
            String successMessage = String.format("총 %d개의 문제가 추천 완료되었습니다.\n", totalRecommended);
            if (failureMessageBuilder.length() > 0) {
                return successMessage + "그러나 일부 날짜는 실패했습니다:\n" + failureMessageBuilder.toString();
            } else {
                return successMessage;
            }
        } else {
            if (failureMessageBuilder.length() > 0) {
                return "수동 추천에 실패했습니다. \n원인: " + failureMessageBuilder.toString();
            } else {
                return "수동 추천에 실패했습니다. (추천할 문제가 없거나, 설정이 올바르지 않습니다.)";
            }
        }
    }
}
