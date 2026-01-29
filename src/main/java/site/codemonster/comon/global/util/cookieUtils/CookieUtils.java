package site.codemonster.comon.global.util.cookieUtils;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import site.codemonster.comon.domain.auth.constant.AuthConstant;
import site.codemonster.comon.global.globalConfig.DomainProperties;

import static site.codemonster.comon.domain.auth.constant.AuthConstant.ACCESS_TOKEN;
import static site.codemonster.comon.domain.auth.constant.AuthConstant.REFRESH_TOKEN;

@Slf4j
@Component
@RequiredArgsConstructor
public class CookieUtils {

    private String domain;
    @Value("${spring.cookie.secure}")
    private boolean secure;
    @Value("${spring.cookie.same}")
    private String same;
    private final DomainProperties domainProperties;

    public String createCookieForRefreshToken(String value) {

        return ResponseCookie.from(REFRESH_TOKEN, value)
                .path("/")
                .secure(secure)
                .maxAge(25 * 60 * 60)
                .domain(domainProperties.getDomainName())
                .httpOnly(true)
                .sameSite(same).build().toString();

    }

    public String createCookieForAccessToken(String value) {

        return ResponseCookie.from(ACCESS_TOKEN, value)
                .path("/")
                .secure(secure)
                .maxAge(25 * 60 * 60)
                .domain(domainProperties.getDomainName())
                .httpOnly(true)
                .sameSite(same).build().toString();
    }

    public void clearCookie(HttpServletResponse response) {
        String refreshCookie = ResponseCookie.from(REFRESH_TOKEN, null)
                .path("/")
                .secure(secure)
                .maxAge(0)
                .domain(domainProperties.getDomainName())
                .httpOnly(true)
                .sameSite(same).build().toString();
        response.addHeader("Set-Cookie", refreshCookie);

        String accessCookie = ResponseCookie.from(ACCESS_TOKEN, null)
                .path("/")
                .secure(secure)
                .maxAge(0)
                .domain(domainProperties.getDomainName())
                .httpOnly(true)
                .sameSite(same).build().toString();

        response.addHeader("Set-Cookie", accessCookie);

    }

    public String checkRefreshTokenInCookie(HttpServletRequest request) {
        String refresh = null;
        Cookie[] cookies = request.getCookies();

        if(cookies != null){
            for (Cookie cookie : cookies) {

                if (cookie.getName().equals(REFRESH_TOKEN)) {

                    refresh = cookie.getValue();
                }
            }
        }
        return refresh;
    }

    public String checkAccessTokenInCookie(HttpServletRequest request) {
        String access = null;
        Cookie[] cookies = request.getCookies();

        if(cookies != null){
            for (Cookie cookie : cookies) {

                if (cookie.getName().equals(ACCESS_TOKEN)) {

                    access = cookie.getValue();
                }
            }
        }
        return access;
    }
}
