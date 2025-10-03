package site.codemonster.comon.global.security.filter;

import site.codemonster.comon.domain.auth.constant.AuthConstant;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.service.MemberService;
import site.codemonster.comon.global.error.ErrorCode;
import site.codemonster.comon.global.error.Member.MemberNotFoundException;
import site.codemonster.comon.global.security.jwt.JWTInformation;
import site.codemonster.comon.global.security.jwt.JWTUtils;
import site.codemonster.comon.global.util.cookieUtils.CookieUtils;
import site.codemonster.comon.global.util.responseUtils.ResponseUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
public class JWTAccessFilter extends OncePerRequestFilter {

    private final JWTUtils jwtUtils;
    private final ResponseUtils responseUtils;
    private final MemberService memberService;
    private final CookieUtils cookieUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isUrlOAuth2(request) || isReissue(request) || filterPassUrl(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = cookieUtils.checkAccessTokenInCookie(request);

        // accessToken 검증
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        //Bearer 부분 제거 후 순수 토큰만 획득
        Optional<ErrorCode> validationToken = jwtUtils.validationToken(token);
        if (validationToken.isPresent()) {
            ErrorCode errorCode = validationToken.get();
            responseUtils.generateErrorResponseInHttpServletResponse(errorCode, response);
            return;
        }

        // 토큰이 access인지 확인 (발급시 페이로드에 명시)
        JWTInformation jwtInformation = jwtUtils.getJWTInformation(token);
        if (!AuthConstant.ACCESS_TOKEN.equals(jwtInformation.category())) {
            responseUtils.generateErrorResponseInHttpServletResponse(ErrorCode.TOKEN_ERROR, response);
            return;
        }

        //토큰에서 username과 role 획득
        Collection<GrantedAuthority> collection = getGrantedAuthorities(jwtInformation);

        Member member = null;

        try {
            member = memberService.getMemberByUUID(jwtInformation.uuid());
        } catch (MemberNotFoundException e) {
                responseUtils.generateErrorResponseInHttpServletResponse(ErrorCode.NOT_COMPLETE_SIGN_UP_ERROR, response);
        }

        if(member.getMemberName() == null)
            responseUtils.generateErrorResponseInHttpServletResponse(ErrorCode.NOT_COMPLETE_SIGN_UP_ERROR, response);

        Authentication authToken = new UsernamePasswordAuthenticationToken(member, null, collection);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private Collection<GrantedAuthority> getGrantedAuthorities(JWTInformation jwtInformation) {
        String role = jwtInformation.role();

        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new SimpleGrantedAuthority(role));
        return collection;
    }

    private boolean isUrlOAuth2(HttpServletRequest request) {
        return request.getRequestURI().contains("oauth2");
    }

    private boolean isReissue(HttpServletRequest request) {
        return request.getRequestURI().contains("api/v1/reissue");
    }

    private boolean filterPassUrl(HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        return requestURI.contains("api/v1/teams/combined")
                || requestURI.contains("api/v1/test/no-auth")
                || requestURI.contains("team-page")
                || requestURI.contains("by-date")
                || (requestURI.contains("subjects") && request.getMethod().equals("GET"))
                || (request.getRequestURI().contains("api/v1/recruitments") && request.getMethod().equals("GET"))
                || requestURI.contains("api/v1/teams/search")
                || requestURI.startsWith("/admin/")
                || requestURI.equals("/actuator/health")
                || requestURI.equals("/admin");
    }
}
