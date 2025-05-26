package site.codemonster.comon.global.util.cookie;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import static site.codemonster.comon.domain.auth.constant.AuthConstant.ACCESS_TOKEN;
import static site.codemonster.comon.domain.auth.constant.AuthConstant.REFRESH_TOKEN;

@Slf4j
public class CookieUtils {

    public static Cookie createCookieWithHttpOnly(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(20 * 60 * 60);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

    public static Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(false);

        return cookie;
    }

    public static void clearCookie(HttpServletResponse response) {
        Cookie accessCookie = new Cookie(ACCESS_TOKEN, null);
        Cookie refreshCookie = new Cookie(REFRESH_TOKEN, null);
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);
    }

    public static String checkRefreshTokenInCookie(HttpServletRequest request) {
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
}
