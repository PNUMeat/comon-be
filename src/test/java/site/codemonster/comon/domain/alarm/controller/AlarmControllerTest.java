package site.codemonster.comon.domain.alarm.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.alarm.entity.Alarm;
import site.codemonster.comon.domain.alarm.dto.AlarmResponse;
import site.codemonster.comon.domain.alarm.repository.AlarmRepository;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.domain.util.TestSecurityContextInjector;
import site.codemonster.comon.domain.util.TestUtil;
import site.codemonster.comon.global.error.ErrorCode;
import site.codemonster.comon.global.error.dto.response.ApiResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AlarmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AlarmRepository alarmRepository;

    @Test
    @DisplayName("알람 목록 조회 성공")
    void getAlarmsSuccess() throws Exception {
        Member member = memberRepository.save(TestUtil.createMember());
        Member otherMember = memberRepository.save(TestUtil.createOtherMember());

        alarmRepository.save(new Alarm("첫 번째 알람", "첫 번째 내용", member));
        alarmRepository.save(new Alarm("두 번째 알람", "두 번째 내용", member));
        alarmRepository.save(new Alarm("다른 회원 알람", "다른 회원 내용", otherMember));

        TestSecurityContextInjector.inject(member);

        String response = mockMvc.perform(get("/api/alarms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<List<AlarmResponse>> apiResponse = objectMapper.readValue(
                response,
                new TypeReference<ApiResponse<List<AlarmResponse>>>() {
                }
        );

        List<AlarmResponse> data = apiResponse.getData();

        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(data).hasSize(2);
            softly.assertThat(data.get(0).title()).isEqualTo("두 번째 알람");
            softly.assertThat(data.get(0).content()).isEqualTo("두 번째 내용");
            softly.assertThat(data.get(1).title()).isEqualTo("첫 번째 알람");
            softly.assertThat(data.get(1).content()).isEqualTo("첫 번째 내용");
        });
    }

    @Test
    @DisplayName("알람 목록 조회 실패 - 인증되지 않은 사용자")
    void getAlarmsFailWhenUnauthorized() throws Exception {
        String response = mockMvc.perform(get("/api/alarms")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<Void> apiResponse = objectMapper.readValue(
                response,
                new TypeReference<ApiResponse<Void>>() {
                }
        );

        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.ERROR);
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            softly.assertThat(apiResponse.getMessage()).isEqualTo(ErrorCode.UNAUTHORIZED_MEMBER_ERROR.getMessage());
        });
    }
}
