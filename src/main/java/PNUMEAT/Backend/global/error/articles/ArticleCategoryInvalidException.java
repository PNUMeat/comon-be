package PNUMEAT.Backend.global.error.articles;

import PNUMEAT.Backend.global.error.ComonException;

import static PNUMEAT.Backend.global.error.ErrorCode.ARTICLE_CATEGORY_INVALID_ERROR;

public class ArticleCategoryInvalidException extends ComonException {
    public ArticleCategoryInvalidException(){
        super(ARTICLE_CATEGORY_INVALID_ERROR);
    }
}
