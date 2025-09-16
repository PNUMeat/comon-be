package site.codemonster.comon.domain.recommendation.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import site.codemonster.comon.domain.adminAuth.entity.AdminMember;
import site.codemonster.comon.domain.problem.enums.Platform; // Platform enum을 import합니다.
import java.util.Arrays;

import static site.codemonster.comon.domain.adminAuth.util.SessionConst.*;

@Slf4j
@Controller
@RequestMapping("/admin/recommendations")
@RequiredArgsConstructor
public class AdminRecommendationPageController {

    // 관리자 페이지 - 문제 추천 설정 페이지
    @GetMapping({"", "/settings"})
    public String recommendationSettings(Model model, HttpSession session) {
        addAdminInfoToModel(model, session);

        model.addAttribute("platforms", Arrays.asList(Platform.values()));

        return "admin/problem-recommendation";
    }

    private void addAdminInfoToModel(Model model, HttpSession session) {
        AdminMember adminMember = (AdminMember) session.getAttribute(ADMIN_SESSION_KEY);
        if (adminMember != null) {
            model.addAttribute("adminMember", adminMember);
            model.addAttribute("adminName", adminMember.getName());
        }
    }
}
