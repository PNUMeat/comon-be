package site.codemonster.comon.global.util.cookieUtils;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.codemonster.comon.domain.auth.constant.AuthConstant;
import site.codemonster.comon.global.globalConfig.DomainProperties;

import static site.codemonster.comon.domain.auth.constant.AuthConstant.ACCESS_TOKEN;
import static site.codemonster.comon.domain.auth.constant.AuthConstant.REFRESH_TOKEN;

@Slf4j
@Component
@RequiredArgsConstructor
public class CookieUtils {

    private final DomainProperties domainProperties;

    public Cookie createCookieForRefreshToken(String value) {
        Cookie cookie = new Cookie(REFRESH_TOKEN, value);
        cookie.setMaxAge(20 * 60 * 60);
        cookie.setSecure(true);
        cookie.setDomain(domainProperties.getDomainName());
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

    public Cookie createCookieForAccessToken(String value) {
        Cookie cookie = new Cookie(ACCESS_TOKEN, value);
        cookie.setMaxAge(24 * 60);
        cookie.setDomain(domainProperties.getDomainName());
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

    public void clearCookie(HttpServletResponse response) {
        Cookie accessCookie = new Cookie(ACCESS_TOKEN, null);
        Cookie refreshCookie = new Cookie(REFRESH_TOKEN, null);
        accessCookie.setDomain(domainProperties.getDomainName());
        accessCookie.setSecure(true);
        accessCookie.setHttpOnly(true);
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        refreshCookie.setDomain(domainProperties.getDomainName());
        refreshCookie.setSecure(true);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);
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
