package site.codemonster.comon.domain.problem.service;

import site.codemonster.comon.domain.problem.collector.ProblemCollector;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.problem.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CollectorService {

    private final ProblemRepository problemRepository;
    private final List<ProblemCollector> collectors; // 모든 Collector들 자동 주입

    public List<Problem> collectNewProblems(Platform platform, int limit) {
        log.info("{}에서 신규 문제 {}개 수집 시작", platform.getName(), limit);

        ProblemCollector collector = findCollectorByPlatform(platform);
        if (collector == null) {
            log.error("{}에 대한 Collector를 찾을 수 없습니다", platform.getName());
            return List.of();
        }

        try {
            List<Problem> newProblems = collector.collectNewProblems(limit);

            if (!newProblems.isEmpty()) {
                List<Problem> savedProblems = problemRepository.saveAll(newProblems);
                log.info("{}에서 {}개 문제 수집 및 저장 완료", platform.getName(), savedProblems.size());
                return savedProblems;
            } else {
                log.info("{}에서 수집할 신규 문제가 없습니다", platform.getName());
                return List.of();
            }

        } catch (Exception e) {
            log.error("{}에서 문제 수집 중 오류 발생", platform.getName(), e);
            throw new RuntimeException("문제 수집 실패: " + e.getMessage());
        }
    }

    public Map<Platform, List<Problem>> collectAllNewProblems(int limitPerPlatform) {
        log.info("모든 플랫폼에서 신규 문제 수집 시작 (플랫폼당 {}개)", limitPerPlatform);

        return collectors.stream()
                .collect(Collectors.toMap(
                        ProblemCollector::getPlatformName,
                        collector -> {
                            try {
                                return collectNewProblems(collector.getPlatformName(), limitPerPlatform);
                            } catch (Exception e) {
                                log.error("{}에서 수집 실패", collector.getPlatformName().getName(), e);
                                return List.of();
                            }
                        }
                ));
    }

    public boolean hasNewProblems(Platform platform) {
        ProblemCollector collector = findCollectorByPlatform(platform);
        if (collector == null) {
            return false;
        }

        try {
            return collector.hasNewProblems();
        } catch (Exception e) {
            log.error("{}의 신규 문제 확인 중 오류", platform.getName(), e);
            return false;
        }
    }

    public Map<Platform, Long> getProblemStatistics() {
        return Map.of(
                Platform.BAEKJOON, problemRepository.countByPlatform(Platform.BAEKJOON),
                Platform.LEETCODE, problemRepository.countByPlatform(Platform.LEETCODE),
                Platform.PROGRAMMERS, problemRepository.countByPlatform(Platform.PROGRAMMERS)
        );
    }

    private ProblemCollector findCollectorByPlatform(Platform platform) {
        return collectors.stream()
                .filter(collector -> collector.getPlatformName() == platform)
                .findFirst()
                .orElse(null);
    }
}
