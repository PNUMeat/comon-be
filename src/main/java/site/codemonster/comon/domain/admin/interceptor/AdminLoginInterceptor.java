package site.codemonster.comon.domain.admin.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import site.codemonster.comon.domain.admin.controller.AdminAuthController;
import site.codemonster.comon.domain.admin.entity.AdminMember;

@Slf4j
@Component
public class AdminLoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        log.debug("관리자 요청 - Method: {}, URI: {}", method, requestURI);

        // 로그인 페이지와 로그인/로그아웃 처리는 제외
        if (isExcludedPath(requestURI)) {
            return true;
        }

        // 세션 체크
        HttpSession session = request.getSession(false);
        if (session == null) {
            log.info("관리자 미인증 요청 (세션 없음) - URI: {}", requestURI);
            redirectToLogin(response, requestURI);
            return false;
        }

        // 세션에서 관리자 정보 확인
        AdminMember adminMember = (AdminMember) session.getAttribute(AdminAuthController.ADMIN_SESSION_KEY);
        if (adminMember == null) {
            log.info("관리자 세션 정보 없음 - URI: {}", requestURI);
            redirectToLogin(response, requestURI);
            return false;
        }

        // 비활성화된 관리자 체크
        if (!adminMember.isActive()) {
            log.warn("비활성화된 관리자 접근 시도 - ID: {}, URI: {}", adminMember.getAdminId(), requestURI);
            session.invalidate();
            redirectToLogin(response, requestURI);
            return false;
        }

        log.debug("관리자 인증 성공 - ID: {}, 이름: {}, URI: {}",
                adminMember.getAdminId(), adminMember.getName(), requestURI);

        // 요청 속성에 관리자 정보 추가 (필요시 Controller에서 사용 가능)
        request.setAttribute("adminMember", adminMember);

        return true;
    }

    /**
     * 제외할 경로인지 확인
     */
    private boolean isExcludedPath(String requestURI) {
        return requestURI.equals("/admin/login") ||
                requestURI.equals("/admin/logout") ||
                requestURI.startsWith("/admin/css/") ||
                requestURI.startsWith("/admin/js/") ||
                requestURI.startsWith("/admin/images/") ||
                requestURI.startsWith("/admin/favicon");
    }

    /**
     * 로그인 페이지로 리다이렉트
     */
    private void redirectToLogin(HttpServletResponse response, String originalUri) throws Exception {
        if (isAjaxRequest(originalUri)) {
            // AJAX 요청인 경우 401 응답
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"로그인이 필요합니다.\",\"redirectUrl\":\"/admin/login\"}");
        } else {
            // 일반 요청인 경우 리다이렉트
            response.sendRedirect("/admin/login");
        }
    }

    /**
     * AJAX 요청인지 확인
     */
    private boolean isAjaxRequest(String requestURI) {
        return requestURI.contains("/api/") || requestURI.endsWith(".json");
    }
}
