package site.codemonster.comon.domain.problem.controller;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import site.codemonster.comon.domain.adminAuth.controller.AdminAuthPageController;
import site.codemonster.comon.domain.adminAuth.entity.AdminMember;
import site.codemonster.comon.domain.problem.dto.request.ProblemInfoRequest;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.problem.service.ProblemQueryService;

@Slf4j
@Controller
@RequestMapping("/admin/problems")
@RequiredArgsConstructor
public class AdminProblemPageController {

    private final ProblemQueryService problemQueryService;

    // 관리자 메인 페이지
    @GetMapping
    public String adminMain(Model model, HttpSession session) {
        return dashboard(model, session);
    }

    // 관리자 대시보드 페이지
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        addAdminInfoToModel(model, session);

        // 플랫폼별 문제 통계
        Map<Platform, Long> statistics = problemQueryService.getProblemStatistics();
        model.addAttribute("statistics", statistics);

        // 전체 문제 수
        long totalProblems = statistics.values().stream().mapToLong(Long::longValue).sum();
        model.addAttribute("totalProblems", totalProblems);

        // 각 플랫폼별 백분율
        model.addAttribute("baekjoonPercent", totalProblems > 0 ? (statistics.get(Platform.BAEKJOON) * 100.0 / totalProblems) : 0);
        model.addAttribute("programmersPercent", totalProblems > 0 ? (statistics.get(Platform.PROGRAMMERS) * 100.0 / totalProblems) : 0);
        model.addAttribute("leetcodePercent", totalProblems > 0 ? (statistics.get(Platform.LEETCODE) * 100.0 / totalProblems) : 0);

        return "admin/dashboard";
    }

    // 관리자 문제 등록 페이지
    @GetMapping("/register")
    public String registerPage(Model model, HttpSession session) {
        addAdminInfoToModel(model, session);
        model.addAttribute("platforms", Platform.values());
        model.addAttribute("request", new ProblemInfoRequest());
        return "admin/problem-register";
    }

    // 관리자 문제 목록 페이지
    @GetMapping("/list")
    public String problemList(Model model, HttpSession session) {
        addAdminInfoToModel(model, session);
        return "admin/problem-list";
    }

    // 모든 페이지에 관리자 정보 추가
    private void addAdminInfoToModel(Model model, HttpSession session) {
        AdminMember adminMember = (AdminMember) session.getAttribute(AdminAuthPageController.ADMIN_SESSION_KEY);
        if (adminMember != null) {
            model.addAttribute("adminMember", adminMember);
            model.addAttribute("adminName", adminMember.getName());
        }
    }
}
