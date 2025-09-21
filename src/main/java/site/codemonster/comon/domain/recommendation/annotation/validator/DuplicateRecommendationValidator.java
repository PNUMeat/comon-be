package site.codemonster.comon.domain.recommendation.annotation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import site.codemonster.comon.domain.recommendation.annotation.DuplicateRecommendation;
import site.codemonster.comon.domain.recommendation.dto.request.PlatformRecommendationRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DuplicateRecommendationValidator implements ConstraintValidator<DuplicateRecommendation, List<PlatformRecommendationRequest>> {
    @Override
    public boolean isValid(List<PlatformRecommendationRequest> requests, ConstraintValidatorContext constraintValidatorContext) {

        Set<String> combinations = new HashSet<>();
        for (PlatformRecommendationRequest request : requests) {
            String combination = request.platform().name() + "-" + request.problemStep().name();
            if (!combinations.add(combination)) {
                return false;
            }
        }

        return true;
    }
}
