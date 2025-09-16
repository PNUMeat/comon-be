package site.codemonster.comon.domain.adminAuth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.codemonster.comon.domain.adminAuth.dto.AdminLoginRequest;
import site.codemonster.comon.domain.adminAuth.entity.AdminMember;
import site.codemonster.comon.domain.adminAuth.service.AdminService;

import static site.codemonster.comon.domain.adminAuth.util.SessionConst.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAuthPageController {

    private final AdminService adminService;

    @Value("${admin.session.timeout:7200}")
    private int sessionTimeout;

    @GetMapping
    public String adminHome() {
        return "redirect:/admin/login";
    }

    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        if (session.getAttribute(ADMIN_SESSION_KEY) != null)
            return "redirect:/admin/problems";

        model.addAttribute("loginRequest", new AdminLoginRequest());
        return "admin/admin-login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginRequest") AdminLoginRequest request,
                        BindingResult bindingResult,
                        HttpSession session,
                        Model model) {

        if (bindingResult.hasErrors()) {
            return "admin/admin-login";
        }

        try {
            AdminMember adminMember = adminService.authenticateAdmin(request);
            session.setAttribute(ADMIN_SESSION_KEY, adminMember);
            session.setMaxInactiveInterval(sessionTimeout);

            return "redirect:/admin/problems";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/admin-login";
        } catch (Exception e) {
            model.addAttribute("error", "로그인 처리 중 오류가 발생했습니다. 관리자에게 문의해주세요.");
            return "admin/admin-login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes redirectAttributes) {

        HttpSession session = request.getSession(false); // 세션이 없다면 생성하지 않음
        if (session == null) return "redirect:/admin/login";

        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "로그아웃되었습니다.");
        return "redirect:/admin/login";
    }
}
