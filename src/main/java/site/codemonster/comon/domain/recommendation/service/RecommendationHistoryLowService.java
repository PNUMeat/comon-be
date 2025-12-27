package site.codemonster.comon.domain.recommendation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.recommendation.entity.RecommendationHistory;
import site.codemonster.comon.domain.recommendation.repository.RecommendationHistoryRepository;
import site.codemonster.comon.domain.team.entity.Team;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
public class RecommendationHistoryLowService {

    private final RecommendationHistoryRepository recommendationHistoryRepository;


    public List<RecommendationHistory> findByLocalDate(LocalDate date) {

       return recommendationHistoryRepository.findByLocalDate(date);
    }

    public List<RecommendationHistory> findByTeamId(Long teamId) {
        return recommendationHistoryRepository.findByTeamId(teamId);
    }

    public void saveAll(List<RecommendationHistory> recommendationHistorys) {
        recommendationHistoryRepository.saveAll(recommendationHistorys);
    }

    public void deleteByTeamId(Long teamId) {
        recommendationHistoryRepository.deleteByTeamTeamId(teamId);
    }
}
