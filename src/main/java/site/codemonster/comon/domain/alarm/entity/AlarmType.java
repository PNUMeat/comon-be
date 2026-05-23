package site.codemonster.comon.domain.alarm.entity;

import lombok.Getter;

@Getter
public enum AlarmType {

    ARTICLE_COMMENT_ALARM("게시글에 댓글이 달렸습니다.");

    private final String message;

    AlarmType(String message) {
        this.message = message;
    }
}
