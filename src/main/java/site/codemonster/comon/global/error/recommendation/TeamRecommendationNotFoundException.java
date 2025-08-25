package site.codemonster.comon.global.error.recommendation;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class TeamRecommendationNotFoundException extends ComonException {
    public TeamRecommendationNotFoundException() {
        super(ErrorCode.TEAM_RECOMMENDATION_NOT_FOUND);
    }
}
