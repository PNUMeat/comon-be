package site.codemonster.comon.domain.recommendation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.recommendation.entity.RecommendationHistory;
import site.codemonster.comon.domain.recommendation.repository.RecommendationHistoryRepository;
import site.codemonster.comon.domain.team.entity.Team;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationHistoryService {
    private final RecommendationHistoryRepository recommendationHistoryRepository;

    @Transactional
    public void saveRecommendationHistory(Team team, List<Problem> problems, LocalDate date) {
        List<RecommendationHistory> histories = problems.stream()
                .map(problem -> RecommendationHistory.of(team, problem, date))
                .collect(Collectors.toList());
        recommendationHistoryRepository.saveAll(histories);
    }

    public Set<Long> getRecommendedProblemIds(Team team, Platform platform) {
        return recommendationHistoryRepository
                .findRecommendedProblemIdsByTeamAndPlatform(team.getTeamId(), platform);
    }
}
