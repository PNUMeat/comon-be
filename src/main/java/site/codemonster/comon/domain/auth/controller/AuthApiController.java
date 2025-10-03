package site.codemonster.comon.domain.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import site.codemonster.comon.domain.auth.constant.AuthConstant;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.service.RefreshTokenService;
import site.codemonster.comon.global.error.dto.response.ApiResponse;
import site.codemonster.comon.global.log.annotation.Trace;
import site.codemonster.comon.global.security.jwt.JWTInformation;
import site.codemonster.comon.global.security.jwt.JWTUtils;
import site.codemonster.comon.global.util.cookieUtils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Trace
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthApiController {

    private final JWTUtils jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtils cookieUtils;

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal Member member,
            HttpServletResponse response
    ){
        refreshTokenService.deleteTokenByMember(member);
        cookieUtils.clearCookie(response);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage("로그아웃이 성공적으로 처리되었습니다."));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<Void>> reissue(HttpServletRequest request, HttpServletResponse response) {

        String refresh = (String) request.getAttribute(AuthConstant.REFRESH_TOKEN);
        JWTInformation jwtInformation = jwtUtil.getJWTInformation(refresh);
        String newAccess = jwtUtil.generateAccessToken(jwtInformation.uuid(), jwtInformation.role());
        String newRefresh = jwtUtil.generateRefreshToken(jwtInformation.uuid(), jwtInformation.role());

        refreshTokenService.renewalRefreshToken(refresh, newRefresh, jwtUtil.getREFRESH_TOKEN_TIME());

        response.setHeader(AuthConstant.AUTHORIZATION, AuthConstant.BEARER + newAccess);
        response.addCookie(cookieUtils.createCookieForAccessToken(newAccess));
        response.addCookie(cookieUtils.createCookieForRefreshToken(newRefresh));

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage("토큰이 정상적으로 재발급 되었습니다."));
    }
}
