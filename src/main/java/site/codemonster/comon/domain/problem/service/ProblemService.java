package site.codemonster.comon.domain.problem.service;

import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.problem.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemService {

    private final ProblemRepository problemRepository;

    public Optional<String> findMaxProblemIdByPlatform(Platform platform) {
        try {
            return problemRepository.findByPlatform(platform)
                    .stream()
                    .map(Problem::getPlatformProblemId)
                    .filter(id -> id.matches("\\d+")) // 숫자만 필터링
                    .map(Integer::parseInt)
                    .max(Integer::compareTo)
                    .map(String::valueOf);
        } catch (Exception e) {
            log.error("최대 문제 번호 조회 실패 - 플랫폼: {}", platform, e);
            return Optional.empty();
        }
    }

    public boolean existsProblem(Platform platform, String problemId) {
        return problemRepository.existsByPlatformAndPlatformProblemId(platform, problemId);
    }
}
