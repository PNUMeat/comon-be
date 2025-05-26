package site.codemonster.comon.domain.teamRecruit.controller;

import lombok.Getter;

@Getter
public enum TeamRecruitResponseEnum {

    TEAM_RECRUIT_CREATE("팀 모집글이 성공적으로 생성되었습니다.",201),
    TEAM_RECRUIT_GET_PAGINATION("팀 모집글이 성공적으로 조회되었습니다.",200),
    TEAM_RECRUIT_CHANGE_STATUS("팀 모집글이 상태가 성공적으로 변경되었습니다.",200),
    TEAM_RECRUIT_DELETE("팀 모집글이 성공적으로 삭제되었습니다.",200),
    TEAM_RECRUIT_UPDATE("팀 모집글이 성공적으로 수정되었습니다.",200),
    TEAM_RECRUIT_INVITE("성공적으로 팀에 초대하였습니다.",200);

    private final String message;
    private final int statusCode;

    TeamRecruitResponseEnum(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

}
