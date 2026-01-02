package site.codemonster.comon.global.response;

import lombok.Getter;

@Getter
public enum StreamEventType {
    PROCESSING("스트리밍 처리중"), COMPLETE("스트리밍 종료");

    private final String message;


    StreamEventType(String message) {
        this.message = message;
    }
}
