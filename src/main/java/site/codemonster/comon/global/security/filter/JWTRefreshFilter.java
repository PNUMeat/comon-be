package site.codemonster.comon.global.security.filter;

import site.codemonster.comon.domain.auth.constant.AuthConstant;
import site.codemonster.comon.domain.auth.repository.RefreshTokenRepository;
import site.codemonster.comon.global.error.ErrorCode;
import site.codemonster.comon.global.security.jwt.JWTInformation;
import site.codemonster.comon.global.security.jwt.JWTUtils;
import site.codemonster.comon.global.util.cookie.CookieUtils;
import site.codemonster.comon.global.util.responseUtils.ResponseUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;


@RequiredArgsConstructor
@Slf4j
public class JWTRefreshFilter extends OncePerRequestFilter {

    private final JWTUtils jwtUtils;
    private final RefreshTokenRepository refreshRepository;
    private final ResponseUtils responseUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!isUrlRefresh(request.getRequestURI())) {

            filterChain.doFilter(request, response);
            return;
        }

        String refresh = CookieUtils.checkRefreshTokenInCookie(request);

        if (refresh == null || !refreshRepository.existsByToken(refresh)) {
            responseUtils.generateErrorResponseInHttpServletResponse(ErrorCode.TOKEN_ERROR, response);
            CookieUtils.clearCookie(response);
            return;
        }

        Optional<ErrorCode> validationToken = jwtUtils.validationToken(refresh);
        if (validationToken.isPresent()) {
            responseUtils.generateErrorResponseInHttpServletResponse(ErrorCode.TOKEN_ERROR, response);
            CookieUtils.clearCookie(response);
            return;
        }

        JWTInformation jwtInformation = jwtUtils.getJWTInformation(refresh);
        if (!AuthConstant.REFRESH_TOKEN.equals(jwtInformation.category())) {
            responseUtils.generateErrorResponseInHttpServletResponse(ErrorCode.TOKEN_ERROR, response);
            CookieUtils.clearCookie(response);
            return;
        }
        request.setAttribute(AuthConstant.REFRESH_TOKEN, refresh);

        filterChain.doFilter(request, response);
    }

    private boolean isUrlRefresh(String requestUri) {
        return requestUri.contains("api/v1/reissue");
    }
}
