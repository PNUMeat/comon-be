package site.codemonster.comon.global.validation.annotation;

import site.codemonster.comon.global.validation.validator.NotNullOrBlankValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotNullOrBlankValidator.class)
public @interface NotNullOrBlank {
    String message() default "값은 null이거나 공백만 입력될 수 없습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
