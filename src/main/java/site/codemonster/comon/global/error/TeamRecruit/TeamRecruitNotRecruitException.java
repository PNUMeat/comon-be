package site.codemonster.comon.global.error.TeamRecruit;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class TeamRecruitNotRecruitException extends ComonException {
    public TeamRecruitNotRecruitException(){
        super(ErrorCode.TEAM_RECRUIT_NOT_RECRUIT_ERROR);
    }
}
