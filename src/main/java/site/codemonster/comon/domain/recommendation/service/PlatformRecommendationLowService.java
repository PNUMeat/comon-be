package site.codemonster.comon.domain.recommendation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.recommendation.dto.request.PlatformRecommendationRequest;
import site.codemonster.comon.domain.recommendation.entity.PlatformRecommendation;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.recommendation.repository.PlatformRecommendationRepository;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class PlatformRecommendationLowService {

    private final PlatformRecommendationRepository  platformRecommendationRepository;

    public void saveAll(List<PlatformRecommendationRequest> platformRecommendationRequests,
                        TeamRecommendation teamRecommendation) {

        List<PlatformRecommendation> platformRecommendations = platformRecommendationRequests.stream()
                .map(platformRecommendationRequest ->
                        new PlatformRecommendation(teamRecommendation, platformRecommendationRequest))
                .toList();


        platformRecommendationRepository.saveAll(platformRecommendations);
    }

    public void deleteByTeamRecommendationId(Long teamRecommendationId) {
        platformRecommendationRepository.deleteByTeamRecommendationId(teamRecommendationId);
    }
}
