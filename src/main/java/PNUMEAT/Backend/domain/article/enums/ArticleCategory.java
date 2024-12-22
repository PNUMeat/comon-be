package PNUMEAT.Backend.domain.article.enums;

import PNUMEAT.Backend.global.error.articles.ArticleCategoryInvalidException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public enum ArticleCategory {
    NORMAL(-1, "일반"),
    STUDY_REVIEW(1, "스터디 복습"),
    STUDY_PREVIEW(2, "스터디 예습"),
    STUDY(3, "스터디"),
    CODING_TEST(4, "코딩 테스트");

    private final int code;
    private final String name;

    ArticleCategory(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isSubject(ArticleCategory category){
        if(category.getCode() > 0){
            return true;
        }
        return false;
    }

    public static List<ArticleCategory> getSubjectCategories(){
        return Arrays.stream(ArticleCategory.values())
                .filter(articleCategory -> articleCategory.code > 0)
                .collect(Collectors.toList());
    }

    public static ArticleCategory fromName(String name){
        return Arrays.stream(ArticleCategory.values())
                .filter(category -> category.getName().equals(name))
                .findFirst()
                .orElseThrow(ArticleCategoryInvalidException::new);
    }
}
