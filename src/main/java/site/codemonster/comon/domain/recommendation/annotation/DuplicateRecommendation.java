package site.codemonster.comon.domain.recommendation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import site.codemonster.comon.domain.recommendation.annotation.validator.DuplicateRecommendationValidator;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME) // .CLASS가 기본값
@Documented
@Constraint(validatedBy = DuplicateRecommendationValidator.class)
public @interface DuplicateRecommendation {

    String message() default "중복된 Platform + ProblemStep 조합입니다."; //선언 필수, 사용중
    Class<?>[] groups() default {}; //선언 필수, 사용하지 않음
    Class<? extends Payload>[] payload() default {};
}
