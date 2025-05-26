package site.codemonster.comon.global.security.configuration;

import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.global.security.interceptor.MemberProfileInterceptor;
import site.codemonster.comon.global.security.jwt.JWTUtils;
import site.codemonster.comon.global.util.responseUtils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class InterceptorConfig implements WebMvcConfigurer {

    private final JWTUtils jwtUtils;
    private final MemberRepository memberRepository;
    private final ResponseUtils responseUtils;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MemberProfileInterceptor(jwtUtils, memberRepository, responseUtils))
                .order(1)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/v1/members","/api/v1/auth","/api/v1/reissue","/api/v1/logout");
    }
}
