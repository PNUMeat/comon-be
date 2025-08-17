package site.codemonster.comon.domain.admin.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import site.codemonster.comon.domain.admin.interceptor.AdminLoginInterceptor;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminWebConfig implements WebMvcConfigurer {

    private final AdminLoginInterceptor adminLoginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("관리자 로그인 인터셉터 등록");

        registry.addInterceptor(adminLoginInterceptor)
                .addPathPatterns("/admin/**")  // /admin으로 시작하는 모든 경로
                .excludePathPatterns(
                        // 인증 관련
                        "/admin",            // ← 이것만 추가하면 됩니다!
                        "/admin/login",
                        "/admin/logout",

                        // 정적 리소스
                        "/admin/css/**",
                        "/admin/js/**",
                        "/admin/images/**",
                        "/admin/fonts/**",
                        "/admin/favicon.ico",
                        "/admin/*.ico",

                        // 에러 페이지
                        "/admin/error/**",
                        "/admin/404",
                        "/admin/500"
                )
                .order(0); // 가장 높은 우선순위로 변경 (0이 가장 먼저 실행)
    }
}
