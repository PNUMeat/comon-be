package site.codemonster.comon.global.validation.validator;

import site.codemonster.comon.global.validation.annotation.NotNullOrBlank;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotNullOrBlankValidator implements ConstraintValidator<NotNullOrBlank, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && !value.trim().isEmpty();
    }
}
