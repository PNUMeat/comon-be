package site.codemonster.comon.domain.problem.enums;

import lombok.Getter;

@Getter
public enum Platform {
    BAEKJOON("백준"),
    PROGRAMMERS("프로그래머스"),
    LEETCODE("리트코드");

    private final String name;

    Platform(String name) {
        this.name = name;
    }
}
