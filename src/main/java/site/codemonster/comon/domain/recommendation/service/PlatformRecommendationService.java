package site.codemonster.comon.domain.recommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.recommendation.dto.request.TeamRecommendationRequest;
import site.codemonster.comon.domain.recommendation.entity.PlatformRecommendation;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.recommendation.repository.PlatformRecommendationRepository;

import java.util.List;
import java.util.stream.Collectors;
import site.codemonster.comon.global.util.convertUtils.JsonListConvertUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlatformRecommendationService {

    private final PlatformRecommendationRepository platformRecommendationRepository;
    private final JsonListConvertUtils convertUtils;

    @Transactional
    public void createPlatformRecommendations(TeamRecommendation teamRecommendation, List<TeamRecommendationRequest.PlatformRecommendationSetting> settings) {
        List<PlatformRecommendation> platforms = settings.stream()
                .map(setting -> {
                    PlatformRecommendation platform = PlatformRecommendation.builder()
                            .platform(setting.platform())
                            .difficulties(convertUtils.convertListToJson(setting.difficulties()))
                            .tags(convertUtils.convertListToJson(setting.tags()))
                            .problemCount(setting.problemCount())
                            .enabled(setting.enabled())
                            .build();

                    platform.setTeamRecommendation(teamRecommendation);
                    return platform;
                })
                .collect(Collectors.toList());

        platformRecommendationRepository.saveAll(platforms);
    }

    public List<PlatformRecommendation> findByTeamRecommendation(TeamRecommendation teamRecommendation) {
        return platformRecommendationRepository.findByTeamRecommendation(teamRecommendation);
    }

    @Transactional
    public void deleteByTeamRecommendation(TeamRecommendation teamRecommendation) {
        platformRecommendationRepository.deleteByTeamRecommendation(teamRecommendation);
    }
}
