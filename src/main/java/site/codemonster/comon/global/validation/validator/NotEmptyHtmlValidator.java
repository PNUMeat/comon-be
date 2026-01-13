package site.codemonster.comon.global.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jsoup.Jsoup;
import site.codemonster.comon.global.validation.annotation.NotEmptyHtml;

public class NotEmptyHtmlValidator implements ConstraintValidator<NotEmptyHtml, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {

        boolean isEmpty = Jsoup.parse(value).text().trim().isEmpty();

        if (isEmpty) return false;
        return true;
    }
}
