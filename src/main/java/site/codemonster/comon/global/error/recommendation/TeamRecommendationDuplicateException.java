package site.codemonster.comon.global.error.recommendation;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class TeamRecommendationDuplicateException extends ComonException {
    public TeamRecommendationDuplicateException() {
        super(ErrorCode.TEAM_RECOMMENDATION_DUPLICATE);
    }
}
