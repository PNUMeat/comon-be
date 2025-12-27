package site.codemonster.comon.global.error.TeamRecruit;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class TeamRecruitDuplicateException extends ComonException {
    public TeamRecruitDuplicateException() {
        super(ErrorCode.TEAM_RECRUIT_DUPLICATE_ERROR);;
    }
}
