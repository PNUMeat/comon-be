package site.codemonster.comon.global.response;

import lombok.Getter;

@Getter
public enum ResponseMessageEnum {
    // TEAM
    TEAM_CREATED_SUCCESS("팀이 성공적으로 생성되었습니다.", 201),
    TEAM_TOTAL_DETAILS_SUCCESS("전체 팀을 성공적으로 조회했습니다.",200),
    MY_TEAM_DETAILS_SUCCESS("나의 팀을 성공적으로 조회했습니다.",200),
    TEAM_JOIN_SUCCESS("팀에 성공적으로 가입했습니다.", 200),
    TEAM_ANNOUNCEMENT_UPDATE_SUCCESS("팀 공지가 성공적으로 업데이트되었습니다.", 200),
    TEAM_LEAVE_SUCCESS("팀을 성공적으로 탈퇴했습니다.", 200),
    TEAM_EDIT_SUCCESS("팀 정보가 성공적으로 변경되었습니다.", 200),
    TEAM_DELETE_SUCCESS("팀 삭제가 성공했습니다.",200),

    //MEMBER
    MEMBER_DELETE_SUCCESS("유저가 성공적으로 삭제되었습니다.",200),

    // Image
    PRESIGNED_URL_SUCCESS("이미지 업로드를 위한 사전 서명 URL을 성공적으로 발급했습니다.", 200);


    private final String message;
    private final int statusCode;

    ResponseMessageEnum(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }
}
