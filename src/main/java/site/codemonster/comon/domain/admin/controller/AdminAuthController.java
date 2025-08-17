package site.codemonster.comon.domain.admin.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.codemonster.comon.domain.admin.dto.AdminLoginRequest;
import site.codemonster.comon.domain.admin.entity.AdminMember;
import site.codemonster.comon.domain.admin.service.AdminService;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;

    public static final String ADMIN_SESSION_KEY = "adminMember";

    // application.yml에서 세션 타임아웃 설정값 주입
    @Value("${admin.session.timeout:7200}") // 기본값 2시간(7200초)
    private int sessionTimeout;

    /**
     * 관리자 로그인 페이지
     */
    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        // 이미 로그인된 상태라면 대시보드로 리다이렉트
        if (session.getAttribute(ADMIN_SESSION_KEY) != null) {
            log.debug("이미 로그인된 관리자의 로그인 페이지 접근");
            return "redirect:/admin/problems";
        }

        model.addAttribute("loginRequest", new AdminLoginRequest());
        return "admin/admin-login";
    }

    /**
     * 관리자 로그인 처리
     */
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginRequest") AdminLoginRequest request,
                        BindingResult bindingResult,
                        HttpSession session,
                        RedirectAttributes redirectAttributes,
                        Model model) {

        if (bindingResult.hasErrors()) {
            log.debug("로그인 폼 검증 실패 - ID: {}", request.getAdminId());
            return "admin/admin-login";
        }

        try {
            AdminMember adminMember = adminService.authenticateAdmin(request);

            // 세션에 관리자 정보 저장
            session.setAttribute(ADMIN_SESSION_KEY, adminMember);
            session.setMaxInactiveInterval(sessionTimeout); // 설정값 사용

            log.info("관리자 로그인 성공 - ID: {}, 이름: {}, 세션ID: {}",
                    adminMember.getAdminId(), adminMember.getName(), session.getId());

            return "redirect:/admin/problems";

        } catch (IllegalArgumentException e) {
            log.warn("관리자 로그인 실패 - ID: {}, 사유: {}", request.getAdminId(), e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "admin/admin-login";
        } catch (Exception e) {
            log.error("관리자 로그인 중 예상치 못한 오류 - ID: {}", request.getAdminId(), e);
            model.addAttribute("error", "로그인 처리 중 오류가 발생했습니다. 관리자에게 문의해주세요.");
            return "admin/admin-login";
        }
    }

    /**
     * 관리자 로그아웃
     */
    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        AdminMember adminMember = (AdminMember) session.getAttribute(ADMIN_SESSION_KEY);

        if (adminMember != null) {
            log.info("관리자 로그아웃 - ID: {}, 세션ID: {}", adminMember.getAdminId(), session.getId());
        } else {
            log.debug("세션 정보 없이 로그아웃 시도");
        }

        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "로그아웃되었습니다.");

        return "redirect:/admin/login";
    }

    /**
     * 관리자 홈 (로그인 후 기본 페이지)
     * /admin 접속시 무조건 로그인 페이지로 리다이렉트
     */
    @GetMapping
    public String adminHome() {
        return "redirect:/admin/login";  // 항상 로그인 페이지로!
    }
}
