package site.codemonster.comon.global.security.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import site.codemonster.comon.global.security.interceptor.AdminPageInterceptor;

@Configuration
@RequiredArgsConstructor
public class AdminWebConfig implements WebMvcConfigurer {

    private final AdminPageInterceptor adminLoginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminLoginInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                        // 관리자 로그인 페이지
                        "/admin",
                        "/admin/login",
                        "/admin/logout",

                        // 정적 리소스
                        "/admin/css/**",
                        "/admin/js/**",
                        "/admin/images/**",

                        // API
                        "/admin/problems/**"
                )
                .order(0);
    }
}
