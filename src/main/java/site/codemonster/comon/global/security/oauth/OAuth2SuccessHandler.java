package site.codemonster.comon.global.security.oauth;

import site.codemonster.comon.domain.auth.entity.RefreshToken;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.domain.auth.service.RefreshTokenService;
import site.codemonster.comon.global.security.jwt.JWTUtils;
import site.codemonster.comon.global.util.cookie.CookieUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import static site.codemonster.comon.domain.auth.constant.AuthConstant.ACCESS_TOKEN;
import static site.codemonster.comon.domain.auth.constant.AuthConstant.REFRESH_TOKEN;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtils jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2UserImpl customUserDetails = (OAuth2UserImpl) authentication.getPrincipal();

        String uuid = customUserDetails.getUUID();

        String role = getRole(authentication);

//        Optional<Member> findMember = memberRepository.findByUuid(uuid);

        Optional<RefreshToken> findRefreshToken = refreshTokenService.findRefreshToken(customUserDetails.getMember().getId());

        String refreshToken = jwtUtil.generateRefreshToken(uuid, role);

        if (findRefreshToken.isEmpty()) {
            refreshTokenService.addRefreshEntity(refreshToken, uuid, jwtUtil.getREFRESH_TOKEN_TIME());
        } else {
            refreshTokenService.renewalRefreshToken(findRefreshToken.get().getToken(), refreshToken, jwtUtil.getREFRESH_TOKEN_TIME());
        }

        String accessToken = jwtUtil.generateAccessToken(uuid, role);

        setInformationInResponse(response, accessToken, refreshToken);

        if(customUserDetails.getMemberName() == null){
            response.sendRedirect("https://codemonster.site/enroll");
        }else{
            response.sendRedirect("https://codemonster.site/");
        }
    }

    private void setInformationInResponse(HttpServletResponse response, String accessToken, String refreshToken) {
        Cookie access = CookieUtils.createCookie(ACCESS_TOKEN, accessToken);
        Cookie refresh = CookieUtils.createCookieWithHttpOnly(REFRESH_TOKEN, refreshToken);

        response.addCookie(access);
        response.addCookie(refresh);
    }

    private String getRole(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        return auth.getAuthority();
    }
}
