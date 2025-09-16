package site.codemonster.comon.global.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import site.codemonster.comon.domain.adminAuth.util.SessionConst;
import site.codemonster.comon.domain.adminAuth.entity.AdminMember;

@Component
public class AdminPageInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        if (isExcludedPath(requestURI))
            return true;

        HttpSession session = request.getSession(false);
        if (session == null) {
            redirectToLogin(response);
            return false;
        }

        AdminMember adminMember = (AdminMember) session.getAttribute(SessionConst.ADMIN_SESSION_KEY);
        if (adminMember == null) {
            redirectToLogin(response);
            return false;
        }

        request.setAttribute("adminMember", adminMember);

        return true;
    }

    private boolean isExcludedPath(String requestURI) {
        return requestURI.equals("/admin/login") ||
                requestURI.equals("/admin/logout") ||
                requestURI.startsWith("/admin/css/") ||
                requestURI.startsWith("/admin/js/") ||
                requestURI.startsWith("/admin/images/") ||
                requestURI.startsWith("/admin/favicon");
    }

    private void redirectToLogin(HttpServletResponse response) throws Exception {
        response.sendRedirect("/admin/login");
    }
}
