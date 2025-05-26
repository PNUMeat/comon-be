package site.codemonster.comon.global.error.articles;

import site.codemonster.comon.global.error.ComonException;

import static site.codemonster.comon.global.error.ErrorCode.ARTICLE_CATEGORY_INVALID_ERROR;

public class ArticleCategoryInvalidException extends ComonException {
    public ArticleCategoryInvalidException(){
        super(ARTICLE_CATEGORY_INVALID_ERROR);
    }
}
