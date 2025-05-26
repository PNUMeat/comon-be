package site.codemonster.comon.global.error.TeamRecruit;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class TeamRecruitNotFoundException extends ComonException{
    public TeamRecruitNotFoundException(){
        super(ErrorCode.TEAM_RECRUIT_NOT_FOUND_ERROR);
    }
}