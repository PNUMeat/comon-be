package site.codemonster.comon.global.security.interceptor;


import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.global.error.ErrorCode;
import site.codemonster.comon.global.security.jwt.JWTInformation;
import site.codemonster.comon.global.security.jwt.JWTUtils;
import site.codemonster.comon.global.util.responseUtils.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class MemberProfileInterceptor implements HandlerInterceptor {

    private final JWTUtils jwtUtils;
    private final MemberRepository memberRepository;
    private final ResponseUtils responseUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if(passInterceptorUrl(request)){
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (isAuthenticationMember(authentication)) {
            responseUtils.generateErrorResponseInHttpServletResponse(ErrorCode.NOT_COMPLETE_SIGN_UP_ERROR, response);
            return false;
        }

        Optional<Member> member = getMemberFromAuthentication(authentication);
        if(member.isEmpty()){
            responseUtils.generateErrorResponseInHttpServletResponse(ErrorCode.NOT_COMPLETE_SIGN_UP_ERROR, response);
            return false;
        }

        if(member.get().getMemberName() == null){
            responseUtils.generateErrorResponseInHttpServletResponse(ErrorCode.NOT_COMPLETE_SIGN_UP_ERROR, response);
            return false;
        }

        request.getSession().setAttribute("LoginMember", member.get());
        return true;
    }

    private Optional<Member> getMemberFromAuthentication(Authentication authentication) {
        // jwt token 추출
        String token = (String) authentication.getPrincipal();

        JWTInformation jwtInformation = jwtUtils.getJWTInformation(token);

        return memberRepository.findByUuid(jwtInformation.uuid());
    }

    private boolean isAuthenticationMember(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    private boolean passInterceptorUrl(HttpServletRequest request) {
        return request.getRequestURI().contains("api/v1/teams/all")
                || request.getRequestURI().contains("api/v1/test/no-auth")
                || request.getRequestURI().contains("api/v1/test/auth")
                || request.getRequestURI().contains("api/v1/teams/combined")
                || request.getRequestURI().contains("team-page")
                || request.getRequestURI().contains("by-date")
                || (request.getRequestURI().contains("subjects") && request.getMethod().equals("GET")
                || (request.getRequestURI().contains("api/v1/recruitments") && request.getMethod().equals("GET"))
                || (request.getRequestURI().contains("api/v1/teams/search"))
        );
    }
}
