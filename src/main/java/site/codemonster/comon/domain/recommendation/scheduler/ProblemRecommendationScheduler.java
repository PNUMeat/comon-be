//package site.codemonster.comon.domain.recommendation.scheduler;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import site.codemonster.comon.domain.problem.service.TeamRecommendationService;
//import site.codemonster.comon.domain.recommendation.dto.request.ManualRecommendationRequest;
//import site.codemonster.comon.domain.team.entity.Team;
//import site.codemonster.comon.domain.team.repository.TeamRepository;
//
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Slf4j
//@Component
//@EnableScheduling
//@RequiredArgsConstructor
//public class ProblemRecommendationScheduler {
//
//    private final TeamRepository teamRepository;
//    private final TeamRecommendationService recommendationService;
//
//    /**
//     * 매시 정각에 자동 추천 실행 체크
//     */
//    @Scheduled(cron = "0 0 * * * *") // 매시 정각
//    public void executeScheduledRecommendations() {
//        LocalDateTime now = LocalDateTime.now();
//        DayOfWeek currentDayOfWeek = now.getDayOfWeek();
//        int currentHour = now.getHour();
//        LocalDate today = now.toLocalDate();
//
//        log.info("자동 추천 스케줄 체크 시작: {}요일 {}시", currentDayOfWeek, currentHour);
//
//        // 자동 추천이 활성화된 팀들 조회
//        List<Team> eligibleTeams = teamRepository.findAll().stream()
//                .filter(team -> team.getAutoRecommendationEnabled() != null && team.getAutoRecommendationEnabled())
//                .filter(team -> team.getRecommendationAt() != null && team.getRecommendationAt() == currentHour)
//                .filter(team -> team.isRecommendationDay(currentDayOfWeek))
//                .toList();
//
//        log.info("자동 추천 대상 팀 수: {}", eligibleTeams.size());
//
//        for (Team team : eligibleTeams) {
//            try {
//                // 수동 추천 서비스를 재사용
//                ManualRecommendationRequest request = new ManualRecommendationRequest();
//                request.setTeamId(team.getTeamId());
//                request.setSelectedDates(List.of(today));
//
//                var result = recommendationService.executeManualRecommendation(request);
//
//                log.info("팀 '{}' 자동 추천 완료: {}",
//                        team.getTeamName(), result.get("message"));
//
//            } catch (Exception e) {
//                log.error("팀 '{}' 자동 추천 실행 중 오류 발생", team.getTeamName(), e);
//            }
//        }
//
//        log.info("자동 추천 스케줄 완료");
//    }
//
//    /**
//     * 매일 자정에 추천 통계 로그 출력
//     */
//    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
//    public void logDailyRecommendationStats() {
//        LocalDate yesterday = LocalDate.now().minusDays(1);
//
//        // 어제 자동 추천된 팀 수 계산
//        long activeTeamsCount = teamRepository.findAll().stream()
//                .filter(team -> team.getAutoRecommendationEnabled() != null && team.getAutoRecommendationEnabled())
//                .filter(team -> team.isRecommendationDay(yesterday.getDayOfWeek()))
//                .count();
//
//        log.info("어제({}) 자동 추천 활성 팀 수: {}", yesterday, activeTeamsCount);
//    }
//}
