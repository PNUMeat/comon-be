package site.codemonster.comon.domain.recommendation.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.codemonster.comon.domain.recommendation.service.*;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamRecommendationScheduler {

    private final TeamRecommendationHighService teamRecommendationService;

    @Scheduled(cron = "0 0 * * * *")
    public void executeAutoRecommendations() {
        LocalDateTime current = LocalDateTime.now();
        teamRecommendationService.executeAutoRecommendation(current);
    }

}
