package site.codemonster.comon.domain.problem.enums;

import lombok.Getter;

@Getter
public enum Platform {
    BAEKJOON("백준", "https://www.acmicpc.net/problem/"),
    PROGRAMMERS("프로그래머스", "https://school.programmers.co.kr/learn/courses/30/lessons/"),
    LEETCODE("리트코드", "https://leetcode.com/problems/");

    private final String name;
    private final String baseUrl;

    Platform(String name, String baseUrl) {
        this.name = name;
        this.baseUrl = baseUrl;
    }

    public static Platform fromName(String name) {
        for (Platform platform : Platform.values()) {
            if (platform.name.equals(name) || platform.name().equals(name)) {
                return platform;
            }
        }
        throw new IllegalArgumentException("Unknown platform: " + name);
    }
}
