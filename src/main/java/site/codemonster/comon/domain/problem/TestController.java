package site.codemonster.comon.domain.problem;

import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.problem.service.CollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test/problems")
@RequiredArgsConstructor
public class TestController {

    private final CollectorService collectorService;

    /**
     * 백준 문제 수집 테스트
     */
    @PostMapping("/collect/baekjoon")
    public List<Problem> collectBaekjoonProblems(@RequestParam(defaultValue = "5") int limit) {
        return collectorService.collectNewProblems(Platform.BAEKJOON, limit);
    }

    /**
     * 플랫폼별 문제 통계
     */
    @GetMapping("/statistics")
    public Map<Platform, Long> getStatistics() {
        return collectorService.getProblemStatistics();
    }
}
