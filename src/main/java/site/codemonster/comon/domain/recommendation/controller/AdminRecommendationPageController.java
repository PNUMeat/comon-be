package site.codemonster.comon.domain.recommendation.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import site.codemonster.comon.domain.adminAuth.controller.AdminAuthPageController;
import site.codemonster.comon.domain.adminAuth.entity.AdminMember;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.team.service.TeamService;

import java.time.DayOfWeek;
import java.util.Arrays;

@Slf4j
@Controller
@RequestMapping("/admin/recommendations")
@RequiredArgsConstructor
public class AdminRecommendationPageController {

    private final TeamService teamService;

    @GetMapping
    public String recommendationMain(Model model, HttpSession session) {
        return recommendationSettings(model, session);
    }

    /**
     * 문제 추천 설정 페이지
     */
    @GetMapping("/settings")
    public String recommendationSettings(Model model, HttpSession session) {
        addAdminInfoToModel(model, session);

        try {
            // 팀 목록 조회
            var teams = teamService.getAllTeamsForRecommendation();
            model.addAttribute("teams", teams);

            // 플랫폼 목록
            model.addAttribute("platforms", Platform.values());

            // 요일 목록
            model.addAttribute("daysOfWeek", Arrays.asList(DayOfWeek.values()));

            // 시간 옵션 (0-23)
            model.addAttribute("hours", generateHourOptions());

            log.info("문제 추천 설정 페이지 로드 완료");

        } catch (Exception e) {
            log.error("문제 추천 설정 페이지 로드 실패", e);
            model.addAttribute("errorMessage", "페이지 로드 중 오류가 발생했습니다.");
        }

        return "admin/problem-recommendation";
    }

    /**
     * 시간 옵션 생성 (0시~23시)
     */
    private String[] generateHourOptions() {
        String[] hours = new String[24];
        for (int i = 0; i < 24; i++) {
            hours[i] = String.format("%02d:00", i);
        }
        return hours;
    }

    /**
     * 모든 페이지에 관리자 정보 추가
     */
    private void addAdminInfoToModel(Model model, HttpSession session) {
        AdminMember adminMember = (AdminMember) session.getAttribute(AdminAuthPageController.ADMIN_SESSION_KEY);
        if (adminMember != null) {
            model.addAttribute("adminMember", adminMember);
            model.addAttribute("adminName", adminMember.getName());
        }
    }
}
