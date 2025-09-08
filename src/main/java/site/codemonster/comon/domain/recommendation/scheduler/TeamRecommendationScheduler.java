package site.codemonster.comon.domain.recommendation.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.codemonster.comon.domain.article.service.ArticleService;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.service.MemberService;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.recommendation.entity.PlatformRecommendation;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.recommendation.service.PlatformRecommendationService;
import site.codemonster.comon.domain.recommendation.service.RecommendationHistoryService;
import site.codemonster.comon.domain.recommendation.service.TeamRecommendationService;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamRecommendationScheduler {

    private final TeamRecommendationService teamRecommendationService;
    private final PlatformRecommendationService platformRecommendationService;
    private final ArticleService articleService;
    private final RecommendationHistoryService recommendationHistoryService;
    private final MemberService memberService;

    @Value("${app.system-admin-id:1}")
    private Long adminId;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void executeAutoRecommendations() {
        LocalDateTime now = LocalDateTime.now();
        int currentHour = now.getHour();
        DayOfWeek currentDayOfWeek = now.getDayOfWeek();
        LocalDate today = now.toLocalDate();

        List<TeamRecommendation> activeTeamRecommendations =
                teamRecommendationService.getSchedulingActiveTeamRecommendations();

        log.info("자동 추천 실행 - 시간: {}시, 요일: {}, 대상 팀: {}개",
                currentHour, currentDayOfWeek.name(), activeTeamRecommendations.size());

        int successCount = 0;
        for (TeamRecommendation teamRecommendation : activeTeamRecommendations) {
            if (processTeamAutoRecommendation(teamRecommendation, currentHour, currentDayOfWeek, today)) {
                successCount++;
            }
        }

        if (successCount > 0) {
            log.info("자동 추천 완료 - 성공: {}개 팀", successCount);
        }
    }

    private boolean processTeamAutoRecommendation(TeamRecommendation teamRecommendation,
                                                  int currentHour, DayOfWeek currentDayOfWeek, LocalDate today) {

        // 추천 시간 체크
        if (teamRecommendation.getRecommendationAt() != currentHour) {
            return false;
        }

        // 추천 요일 체크
        Set<DayOfWeek> recommendDays = teamRecommendation.getRecommendationDays();
        if (recommendDays == null || !recommendDays.contains(currentDayOfWeek)) {
            return false;
        }

        // 중복 추천 체크
        if (articleService.isRecommendationAlreadyExists(teamRecommendation.getTeam(), today)) {
            return false;
        }

        return executeRecommendationForTeam(teamRecommendation, today);
    }

    private boolean executeRecommendationForTeam(TeamRecommendation teamRecommendation, LocalDate targetDate) {
        String teamName = teamRecommendation.getTeam().getTeamName();

        try {
            Member systemAdmin = memberService.getMemberById(adminId);
            List<PlatformRecommendation> platformRecommendations =
                    platformRecommendationService.findByTeamRecommendation(teamRecommendation);

            List<Problem> recommendedProblems = teamRecommendationService.recommendProblemsForTeam(
                    teamRecommendation.getTeam(), platformRecommendations);

            if (recommendedProblems.isEmpty()) {
                log.warn("팀 [{}]: 추천 가능한 문제 없음", teamName);
                return false;
            }

            String articleTitle = articleService.createRecommendationArticle(
                    teamRecommendation.getTeam(), systemAdmin, recommendedProblems, targetDate);

            recommendationHistoryService.saveRecommendationHistory(
                    teamRecommendation.getTeam(), recommendedProblems, targetDate);

            log.info("팀 [{}] 자동 추천 성공: {} ({}문제)", teamName, articleTitle, recommendedProblems.size());
            return true;

        } catch (Exception e) {
            log.error("팀 [{}] 자동 추천 실패: {}", teamName, e.getMessage());
            return false;
        }
    }
}
