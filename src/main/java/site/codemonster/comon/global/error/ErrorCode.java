package site.codemonster.comon.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    //AUTH
    UNAUTHORIZED_MEMBER_ERROR(HttpStatus.UNAUTHORIZED, 401, "인증 되지 않은 사용자 입니다."),
    NOT_COMPLETE_SIGN_UP_ERROR(HttpStatus.UNAUTHORIZED, 100, "회원가입이 마무리되지 않았습니다."),
    MEMBER_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, 404,"존재하지 않는 유저입니다. 유저를 찾을 수 없습니다."),
    TOKEN_EXPIRED_ERROR(HttpStatus.UNAUTHORIZED, 101, "토큰이 만료되었습니다."),
    TOKEN_ERROR(HttpStatus.UNAUTHORIZED, 401, "잘못된 Token 입니다."),
    FORBIDDEN_MEMBER_ERROR(HttpStatus.FORBIDDEN, 403, "허가 되지 않은 사용자 입니다."),

    //ARTICLE
    ARTICLE_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, 404, "존재 하지 않는 게시글 입니다."),
    MEMBER_NOT_IN_TEAM(HttpStatus.FORBIDDEN,403,"팀에 멤버가 존재하지않습니다."),
    UNAUTHORIZED_ACTION(HttpStatus.FORBIDDEN,403, "게시물의 작성자가 아닙니다."),
    SUBJECT_DUPLICATED_ERROR(HttpStatus.BAD_REQUEST,400, "이미 주제를 작성했습니다."),
    ARTICLE_CATEGORY_INVALID_ERROR(HttpStatus.BAD_REQUEST, 400,"해당 게시물 카테고리가 존재하지 않습니다."),

    //TEAM
    TOPIC_INVALID_ERROR(HttpStatus.BAD_REQUEST,400, "주제가 올바른 형식이 아닙니다."),
    TEAM_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, 404,"팀이 존재하지 않습니다."),
    TEAM_PASSWORD_INVALID(HttpStatus.BAD_REQUEST, 400,"팀 비밀번호가 옳지 않습니다."),
    TEAM_ALREADY_JOIN(HttpStatus.BAD_REQUEST, 400, "이미 팀에 가입했습니다."),
    TEAM_MANAGER_INVALID_ERROR(HttpStatus.BAD_REQUEST, 400, "팀의 매니저가 옳지 않습니다."),
    TEAM_MEMBER_INVALID_ERROR(HttpStatus.BAD_REQUEST, 400, "팀의 멤버가 아닙니다."),
    MEMBER_LIMIT_BELOW_CURRENT_ERROR(HttpStatus.BAD_REQUEST, 400, "인원 제한이 현재 팀원 수보다 작습니다."),
    EXCEED_MAX_MEMBERS(HttpStatus.BAD_REQUEST, 400, "현재 팀의 최대 인원을 넘었습니다."),
    ALREADY_TEAM_MANAGER(HttpStatus.BAD_REQUEST, 400, "이미 팀장 입니다."),
    NOT_TEAM_OWNER(HttpStatus.BAD_REQUEST,400,"팀의 방장이 아닙니다."),
    TEAM_MANAGER_NOT_FOUND(HttpStatus.NOT_FOUND,404,"팀에 팀장이 존재하지 않습니다."),

    //TEAM RECRUIT
    TEAM_RECRUIT_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, 404,"팀 모집글이 존재하지 않습니다."),
    TEAM_RECRUIT_NOT_AUTHOR_ERROR(HttpStatus.BAD_REQUEST, 401,"팀 모집글 작성자가 아닙니다."),
    TEAM_RECRUIT_NOT_RECRUIT_ERROR(HttpStatus.BAD_REQUEST, 400,"현재 모집 중이지 않습니다."),

    //TEAM APPLY
    TEAM_APPLY_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, 404,"팀 지원글이 존재하지 않습니다."),
    TEAM_APPLY_DELETE_FORBIDDEN_ERROR(HttpStatus.FORBIDDEN, 403, "팀 지원글을 삭제할 권한이 없습니다."),
    TEAM_APPLY_UPDATE_FORBIDDEN_ERROR(HttpStatus.FORBIDDEN, 403, "팀 지원글을 수정할 권한이 없습니다."),

    //IMAGE
    IMAGE_FILE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST,400,"이미지 파일 업로드 에러발생."),
    IMAGE_FILE_DELETE_ERROR(HttpStatus.BAD_REQUEST,400,"이미지 파일 삭제 에러발생."),
    S3_NETWORK_ERROR(HttpStatus.BAD_REQUEST,400,"S3 연결 에러 발생"),
    S3_PRESIGNED_URL_ERROR(HttpStatus.BAD_REQUEST,400,"S3 PreSigned URL 생성도중 문제가 발생하였습니다."),
    IMAGE_FILE_NOT_FOUND(HttpStatus.NOT_FOUND,404,"이미지 파일을 찾을 수 없습니다."),
    IMAGE_FILE_TYPE_ERROR(HttpStatus.BAD_REQUEST,400,"이미지 파일 형식이 비어있습니다."),

    //GENERAL
    DATETIME_PARSE_ERROR(HttpStatus.BAD_REQUEST, 400,"날짜 및 날짜 형식이 올바르지 않습니다."),

    // TEAM_RECOMMENDATION
    TEAM_RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "팀의 추천 설정이 존재하지 않습니다."),
    TEAM_RECOMMENDATION_DUPLICATE(HttpStatus.CONFLICT, 400,"이미 팀 추천이 존재합니다"),
    TEAM_RECOMMENDATION_PROBLEM_SHORTAGE(HttpStatus.BAD_REQUEST, 400,"추천하려는 문제 수가 부족합니다"),

    // PROBLEM
    PROBLEM_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, 404, "문제를 찾을 수 없습니다."),
    PROBLEM_INVALID_INPUT_ERROR(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 문제 정보입니다."),
    PROBLEM_COLLECTION_ERROR(HttpStatus.BAD_REQUEST, 400, "문제 정보를 수집할 수 없습니다."),
    PROBLEM_UPDATE_DATA_EMPTY_ERROR(HttpStatus.BAD_REQUEST, 400, "수정할 데이터가 없습니다."),
    PROBLEM_BATCH_REGISTER_EMPTY_ERROR(HttpStatus.BAD_REQUEST, 400, "등록할 문제 목록이 비어있습니다."),
    PROBLEM_REGISTER_INTERRUPTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "문제 등록 중 인터럽트가 발생했습니다."),
    PROBLEM_PLATFORM_REQUIRED_ERROR(HttpStatus.BAD_REQUEST, 400, "플랫폼 정보가 필요합니다."),
    PROBLEM_TITLE_REQUIRED_ERROR(HttpStatus.BAD_REQUEST, 400, "문제 제목이 필요합니다."),
    PROBLEM_ID_REQUIRED_ERROR(HttpStatus.BAD_REQUEST, 400, "문제 ID가 필요합니다."),
    PROBLEM_UNSUPPORTED_FIELD_ERROR(HttpStatus.BAD_REQUEST, 400, "지원하지 않는 필드입니다."),
    PROBLEM_DIFFICULTY_REQUIRED_ERROR(HttpStatus.BAD_REQUEST, 400, "난이도를 선택해주세요."),
    PROBLEM_DIFFICULTY_INVALID_ERROR(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 난이도입니다."),
    PROBLEM_TITLE_TOO_LONG_ERROR(HttpStatus.BAD_REQUEST, 400, "제목이 너무 깁니다. (최대 50자)"),
    PROBLEM_TAGS_TOO_LONG_ERROR(HttpStatus.BAD_REQUEST, 400, "태그가 너무 깁니다. (최대 50자)"),
    PROBLEM_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, 400, "문제 정보 검증에 실패했습니다."),
    PROBLEM_DUPLICATE_ERROR(HttpStatus.CONFLICT, 409, "이미 등록되어있는 문제입니다."),

    // ETC
    INVALID_IMAGE_URL_FORMAT(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 이미지 URL 형식입니다."),
    ;


    private final HttpStatus status;
    private final int customStatusCode;
    private final String message;
}
