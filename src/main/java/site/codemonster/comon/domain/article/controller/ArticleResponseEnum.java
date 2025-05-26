package site.codemonster.comon.domain.article.controller;

import lombok.Getter;

@Getter
public enum ArticleResponseEnum {

    ARTICLE_CREATE_SUCCESS("게시글이 성공적으로 생성되었습니다.",201),
    ARTICLE_DELETE_SUCCESS("게시글이 성공적으로 삭제되었습니다.",200),
    ARTICLE_PUT_SUCCESS("게시글이 성공적으로 수정되었습니다.",200),
    SUBJECT_CREATE_SUCCESS("주제가 성공적으로 생성되었습니다.", 201),
    SUBJECT_DELETE_SUCCESS("주제를 성공적으로 삭제했습니다.", 200),
    SUBJECT_UPDATE_SUCCESS("주제를 성공적으로 업데이트했습니다.", 200),
    GET_ARTICLE_PARTICULAR_TEAM("해당 팀의 게시글을 조회했습니다.", 200),
    GET_MY_PAGE_ARTICLE_PARTICULAR_TEAM("마이페이지에서 해당 팀의 게시글을 조회했습니다.", 200),
    GET_ARTICLE_PARTICULAR_TEAM_AND_DATE("해당 날짜의 해당 팀의 게시글을 조회했습니다.", 200),
    GET_SUBJECT_PARTICULAR_TEAM_AND_DATE("해당 날짜의 해당 팀의 주제를 조회했습니다.", 200),
    PRESINGED_URL_CREATE_SUCCESS("S3 Presigned URL 생성에 성공하였습니다.", 200);

    private final String message;
    private final int statusCode;

    ArticleResponseEnum(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }
}
