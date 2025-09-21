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

    private final TeamRecommendationLowService teamRecommendationLowService;
    private final TeamRecommendationService teamRecommendationService;
    private final RecommendationHistoryLowService recommendationHistoryLowService;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void executeAutoRecommendations() {
        LocalDateTime now = LocalDateTime.now();
        int currentHour = now.getHour();
        DayOfWeek currentDayOfWeek = now.getDayOfWeek();
        LocalDate today = now.toLocalDate();

        // 등록된 모든 TeamRecommendation 조회
        List<TeamRecommendation> teamRecommendations =
                teamRecommendationLowService.findAllWithRecommendationDays();

        // 오늘 이미 추천된 팀들의 PK
        List<Long> alreadyRecommendedTeamIds = recommendationHistoryLowService.findByLocalDate(today)
                .stream()
                .map(recommendationHistory ->
                        recommendationHistory.getTeam().getTeamId()).toList();


        // 추천 요일, 시간이 아닌 팀과 이미 추천된 팀들을 제외한 TeamRecommendation
        List<TeamRecommendation> activeTeamRecommendations = teamRecommendations.stream()
                .filter(teamRecommendation -> {
                    if(!teamRecommendation.getRecommendationAt().equals(currentHour)) return false;

                    List<DayOfWeek> dayOfWeeks = teamRecommendation.getTeamRecommendationDays()
                            .stream().map(TeamRecommendationDay::getDayOfWeek).toList();

                    if (alreadyRecommendedTeamIds.contains(teamRecommendation.getTeam().getTeamId())) return false;

                    if (dayOfWeeks.contains(currentDayOfWeek)) return true;
                    return false;
                }).toList();



        log.info("자동 추천 실행 - 시간: {}시, 요일: {}, 대상 팀: {}개",
                currentHour, currentDayOfWeek.name(), teamRecommendations.size());
        try {
            activeTeamRecommendations.forEach(teamRecommendation -> {
                teamRecommendationService.executeRecommendation(teamRecommendation, today);
            });
        } catch (Exception e) {
            log.info("자동 추천 실패 {}", e.getMessage());
        }

        log.info("자동 추천 성공");

    }

}
