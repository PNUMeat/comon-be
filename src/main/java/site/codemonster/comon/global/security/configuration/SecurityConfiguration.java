package site.codemonster.comon.global.security.configuration;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import site.codemonster.comon.domain.auth.repository.RefreshTokenRepository;
import site.codemonster.comon.domain.auth.service.MemberService;
import site.codemonster.comon.global.error.ErrorCode;
import site.codemonster.comon.global.globalConfig.DomainProperties;
import site.codemonster.comon.global.security.filter.JWTAccessFilter;
import site.codemonster.comon.global.security.filter.JWTRefreshFilter;
import site.codemonster.comon.global.security.jwt.JWTUtils;
import site.codemonster.comon.global.security.oauth.OAuth2SuccessHandler;
import site.codemonster.comon.global.security.oauth.OAuth2UserServiceImpl;
import site.codemonster.comon.global.util.responseUtils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final OAuth2UserServiceImpl oAuth2UserService;

    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    private final JWTUtils jwtUtil;

    private final RefreshTokenRepository refreshTokenRepository;

    private final ResponseUtils responseUtils;

    private final MemberService memberService;

    private final DomainProperties domainProperties;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf((auth) -> auth.disable())
                .formLogin((auth) -> auth.disable())
                .httpBasic((auth) -> auth.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .addFilterAfter(new JWTAccessFilter(jwtUtil, responseUtils, memberService), OAuth2LoginAuthenticationFilter.class)
                .addFilterAfter(new JWTRefreshFilter(jwtUtil, refreshTokenRepository, responseUtils), OAuth2LoginAuthenticationFilter.class)
                .oauth2Login(
                    (oauth2) -> oauth2
                    .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig.userService(oAuth2UserService))
                    .successHandler(oAuth2SuccessHandler)
                )
                .authorizeHttpRequests((auth) -> auth
                    .requestMatchers(
                            "api/v1/test/no-auth",
                            "/api/v1/reissue",
                            "api/v1/teams/combined",
                            "api/v1/teams/{teamId}/team-page",
                            "api/v1/articles/{teamId}/by-date",
                            "api/v1/articles/teams/{teamId}/subjects",
                            "api/v1/teams/search",
                            "/api/v1/recruitments",
                            "/api/v1/recruitments/{recruitId}",
                            "/admin/**",
                            "/api/v1/teams/all",
                            "/actuator/health"
                    ).permitAll()
                    .anyRequest().authenticated())
                .sessionManagement((session) -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e->e
                    .authenticationEntryPoint((request, response, authException)-> {
                        responseUtils.generateErrorResponseInHttpServletResponse(ErrorCode.UNAUTHORIZED_MEMBER_ERROR, response);
                    })
                    .accessDeniedHandler((request, response, authException)-> {
                        responseUtils.generateErrorResponseInHttpServletResponse(ErrorCode.FORBIDDEN_MEMBER_ERROR, response);
                    }));


        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(domainProperties.getBackend(), domainProperties.getFrontend()));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cookie", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(
                        "/favicon.ico",
                        "/error",
                        "/actuator/prometheus",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**",
                        "/api/v1/teams/all"
                );
    }
}
