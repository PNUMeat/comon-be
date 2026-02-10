package site.codemonster.comon.domain.article.controller;

import lombok.Getter;

@Getter
public enum ArticleCommentResponseEnum {

    COMMENT_CREATE_SUCCESS("댓글이 성공적으로 생성되었습니다.", 201),
    COMMENT_LIST_SUCCESS("댓글 목록 조회 성공", 200),
    COMMENT_UPDATE_SUCCESS("댓글이 성공적으로 수정되었습니다.", 200);

    private final String message;
    private final int statusCode;

    ArticleCommentResponseEnum(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }
}
