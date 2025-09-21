package site.codemonster.comon.domain.recommendation.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendationDay;
import site.codemonster.comon.domain.recommendation.service.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamRecommendationScheduler {

    private final TeamRecommendationService teamRecommendationService;

    @Scheduled(cron = "0 0 * * * *")
    public void executeAutoRecommendations() {
        LocalDateTime current = LocalDateTime.now();
        teamRecommendationService.executeAutoRecommendation(current);
    }

}
