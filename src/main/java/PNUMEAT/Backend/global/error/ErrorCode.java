package PNUMEAT.Backend.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    //AUTH
    UNAUTHORIZED_MEMBER_ERROR(HttpStatus.UNAUTHORIZED, "인증 되지 않은 사용자 입니다."),
    MEMBER_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다. 유저를 찾을 수 없습니다."),
    MEMBER_PROFILE_UPDATE_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "프로필 수정할 부분에 값을 입력해 주세요."),
    TOKEN_EXPIRED_ERROR(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    TOKEN_MALFORMED_ERROR(HttpStatus.UNAUTHORIZED, "올바른 Token 형식이 아닙니다."),
    TOKEN_UNSUPPORTED_ERROR(HttpStatus.UNAUTHORIZED, "지원하지 않는 Token 입니다."),
    TOKEN_SIGNATURE_ERROR(HttpStatus.UNAUTHORIZED, "Token 의 서명이 유효하지 않습니다."),
    TOKEN_ERROR(HttpStatus.UNAUTHORIZED, "잘못된 Token 입니다."),

    //ARTICLE
    ARTICLE_FORBIDDEN_ERROR(HttpStatus.FORBIDDEN, "게시글 권한이 없습니다."),
    ARTICLE_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "존재 하지 않는 게시글 입니다."),
    MEMBER_NOT_IN_TEAM(HttpStatus.FORBIDDEN,"팀에 멤버가 존재하지않습니다."),
    UNAUTORIZED_ACTION(HttpStatus.FORBIDDEN,"게시물의 작성자가 아닙니다."),
    SUBJECT_ALREADY_CREATED_ERROR(HttpStatus.CONFLICT, "이미 주제를 작성했습니다."),
    ARTICLE_CATEGORY_INVALID_ERROR(HttpStatus.BAD_REQUEST, "해당 게시물 카테고리가 존재하지 않습니다."),

    //TEAM
    TOPIC_INVALID_ERROR(HttpStatus.BAD_REQUEST, "주제가 올바른 형식이 아닙니다."),
    TEAM_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "팀이 존재하지 않습니다."),
    TEAM_PASSWORD_INVALID(HttpStatus.BAD_REQUEST, "팀 비밀번호가 옳지 않습니다."),
    TEAM_ALREADY_JOIN(HttpStatus.CONFLICT, "이미 팀에 가입했습니다."),
    TEAM_MANAGER_INVALID_ERROR(HttpStatus.BAD_REQUEST, "팀의 매니저가 옳지 않습니다."),

    //IMAGE
    IMAGE_FILE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST,"이미지 파일 업로드 에러발생."),
    IMAGE_FILE_DELETE_ERROR(HttpStatus.BAD_REQUEST,"이미지 파일 삭제 에러발생."),
    S3_NETWORK_ERROR(HttpStatus.BAD_REQUEST,"S3 연결 에러 발생"),

    //GENERAL
    DATETIME_PARSE_ERROR(HttpStatus.BAD_REQUEST, "날짜 및 날짜 형식이 올바르지 않습니다.");

    private final HttpStatus status;

    private final String message;
}
