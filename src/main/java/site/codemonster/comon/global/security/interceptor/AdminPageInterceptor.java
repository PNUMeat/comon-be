package site.codemonster.comon.global.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import site.codemonster.comon.domain.adminAuth.entity.AdminMember;

import static site.codemonster.comon.domain.adminAuth.util.SessionConst.*;

@Component
public class AdminPageInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            redirectToLogin(response);
            return false;
        }

        AdminMember adminMember = (AdminMember) session.getAttribute(ADMIN_SESSION_KEY);
        if (adminMember == null) {
            redirectToLogin(response);
            return false;
        }

        return true;
    }

    private void redirectToLogin(HttpServletResponse response) throws Exception {
        response.sendRedirect("/admin/login");
    }
}
