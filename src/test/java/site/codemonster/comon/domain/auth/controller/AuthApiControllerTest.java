package site.codemonster.comon.domain.auth.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.auth.constant.AuthConstant;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.entity.RefreshToken;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.domain.auth.repository.RefreshTokenRepository;
import site.codemonster.comon.domain.util.TestSecurityContextInjector;
import site.codemonster.comon.domain.util.TestUtil;
import site.codemonster.comon.global.error.ErrorCode;
import site.codemonster.comon.global.error.dto.response.ApiResponse;
import site.codemonster.comon.global.response.ResponseMessageEnum;
import site.codemonster.comon.global.security.jwt.JWTUtils;
import site.codemonster.comon.global.util.cookieUtils.CookieUtils;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.SoftAssertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static site.codemonster.comon.domain.auth.constant.AuthConstant.*;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class AuthApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JWTUtils jwtUtil;

    @Autowired
    private CookieUtils cookieUtils;

    @Test
    @DisplayName("로그아웃 성공")
    void logoutSuccess() throws Exception {
        Member member = memberRepository.save(TestUtil.createMember());
        TestSecurityContextInjector.inject(member);

        String response = mockMvc.perform(post("/api/v1/logout")
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<Void> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<Void>>() {
        });

        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(apiResponse.getMessage()).isEqualTo("로그아웃이 성공적으로 처리되었습니다.");
        });

    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissueSuccess() throws Exception {
        Member member = memberRepository.save(TestUtil.createMember());

        String token = jwtUtil.generateRefreshToken(member.getUuid(), member.getRole());

        RefreshToken oldRefreshToken = refreshTokenRepository.save(new RefreshToken(member, token, jwtUtil.getREFRESH_TOKEN_TIME().toString()));


        String response = mockMvc.perform(post("/api/v1/reissue")
                        .cookie(new Cookie(REFRESH_TOKEN, token)))
                .andExpect(cookie().exists(REFRESH_TOKEN))
                .andExpect(cookie().exists(ACCESS_TOKEN))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<Void> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<Void>>() {
        });

        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(apiResponse.getMessage()).isEqualTo("토큰이 정상적으로 재발급 되었습니다.");
        });
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 리프래시 토큰 존재하지 않음")
    void reissueFail() throws Exception {

        String response = mockMvc.perform(post("/api/v1/reissue"))
                .andExpect(cookie().exists(REFRESH_TOKEN))
                .andExpect(cookie().exists(ACCESS_TOKEN))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<Void> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<Void>>() {
        });


        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.ERROR);
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            softly.assertThat(apiResponse.getMessage()).isEqualTo(ErrorCode.TOKEN_ERROR.getMessage());
        });

    }
}
