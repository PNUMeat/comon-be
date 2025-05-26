package site.codemonster.comon.global.error.articles;


import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class ArticleNotFoundException extends ComonException {
    public ArticleNotFoundException() {
        super(ErrorCode.ARTICLE_NOT_FOUND_ERROR);
    }

}
