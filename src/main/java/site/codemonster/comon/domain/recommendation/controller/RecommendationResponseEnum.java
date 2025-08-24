package site.codemonster.comon.domain.recommendation.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecommendationResponseEnum {

    // 추천 설정 관련
    RECOMMENDATION_SETTINGS_SAVE_SUCCESS(HttpStatus.OK, "문제 추천 설정이 저장되었습니다."),
    RECOMMENDATION_SETTINGS_GET_SUCCESS(HttpStatus.OK, "문제 추천 설정을 조회했습니다."),
    RECOMMENDATION_SETTINGS_RESET_SUCCESS(HttpStatus.OK, "추천 설정이 초기화되었습니다."),

    // 수동 추천 관련
    MANUAL_RECOMMENDATION_SUCCESS(HttpStatus.OK, "수동 추천이 실행되었습니다."),

    // 플랫폼 옵션 조회 관련
    PLATFORM_OPTIONS_GET_SUCCESS(HttpStatus.OK, "플랫폼 옵션을 조회했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return httpStatus.value();
    }
}
