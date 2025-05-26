package site.codemonster.comon.domain.teamApply.controller;

import lombok.Getter;

@Getter
public enum TeamApplyResponseEnum {

    TEAM_APPLY_CREATE("팀 지원글이 성공적으로 생성되었습니다.",201),
    TEAM_APPLY_DELETE("팀 지원글이 성공적으로 삭제되었습니다.",200),
    TEAM_APPLY_UPDATE("팀 지원글이 성공적으로 수정되었습니다.",200);

    private final String message;
    private final int statusCode;

    TeamApplyResponseEnum(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }
}
