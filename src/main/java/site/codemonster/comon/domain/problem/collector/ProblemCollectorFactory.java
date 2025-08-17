package site.codemonster.comon.domain.problem.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.codemonster.comon.domain.problem.enums.Platform;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemCollectorFactory {

    private final BaekjoonCollector baekjoonCollector;
    private final LeetcodeCollector leetcodeCollector;
    private final ProgrammersCollector programmersCollector;

    public ProblemCollector getCollector(Platform platform) {
        return switch (platform) {
            case BAEKJOON -> baekjoonCollector;
            case LEETCODE -> leetcodeCollector;
            case PROGRAMMERS -> programmersCollector;
            default -> throw new IllegalArgumentException("지원하지 않는 플랫폼입니다: " + platform);
        };
    }
}
