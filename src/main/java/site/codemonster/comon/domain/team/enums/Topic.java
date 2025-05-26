package site.codemonster.comon.domain.team.enums;

import site.codemonster.comon.global.error.ComonException;
import lombok.Getter;

import java.util.Arrays;

import static site.codemonster.comon.global.error.ErrorCode.TOPIC_INVALID_ERROR;

@Getter
public enum Topic {
    CODINGTEST(1, "코딩테스트"),
    STUDY(2, "스터디");

    private final int code;
    private final String name;

    Topic(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static Topic fromName(String name){
        return Arrays.stream(Topic.values())
                .filter(topic -> topic.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new ComonException(TOPIC_INVALID_ERROR));
    }
}
