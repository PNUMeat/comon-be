package site.codemonster.comon.global.error.recommendation;

import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class TeamRecommendationProblemShortageException extends ComonException {
    public TeamRecommendationProblemShortageException() {
        super(ErrorCode.TEAM_RECOMMENDATION_PROBLEM_SHORTAGE);
    }
}
