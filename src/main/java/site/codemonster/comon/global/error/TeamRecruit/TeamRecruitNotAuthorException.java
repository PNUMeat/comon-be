package site.codemonster.comon.global.error.TeamRecruit;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class TeamRecruitNotAuthorException extends ComonException {
    public TeamRecruitNotAuthorException(){
        super(ErrorCode.TEAM_RECRUIT_NOT_AUTHOR_ERROR);
    }
}
