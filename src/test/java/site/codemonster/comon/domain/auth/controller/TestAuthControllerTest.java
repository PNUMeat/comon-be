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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.domain.util.TestUtil;
import site.codemonster.comon.global.error.dto.response.ApiResponse;
import site.codemonster.comon.global.security.jwt.JWTUtils;
import site.codemonster.comon.global.util.cookieUtils.CookieUtils;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.SoftAssertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class TestAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CookieUtils cookieUtils;

    @Autowired
    private JWTUtils  jwtUtils;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("액세스 쿠키 테스트")
    void accessTokenCookieTest() throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());
        member.updateProfile("testName", "testDescription", "testImageUrl");

        String accessToken = jwtUtils.generateAccessToken(member.getUuid(), member.getRole());
        Cookie cookieForAccessToken = cookieUtils.createCookieForAccessToken(accessToken);

        String response = mockMvc.perform(get("/api/v1/test/auth")
                        .cookie(cookieForAccessToken)
                )
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);


        ApiResponse<Void> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<Void>>() {
        });

        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(apiResponse.getMessage()).isEqualTo("인증된 회원입니다.");
        });
    }
}
