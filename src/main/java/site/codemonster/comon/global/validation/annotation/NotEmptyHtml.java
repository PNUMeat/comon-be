package site.codemonster.comon.global.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import site.codemonster.comon.global.validation.validator.NotEmptyHtmlValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotEmptyHtmlValidator.class)
public @interface NotEmptyHtml {
    String message() default "빈 값을 입력할 수 없습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
