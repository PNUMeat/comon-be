package site.codemonster.comon.domain.alarm.controller;

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
import site.codemonster.comon.domain.alarm.repository.AlarmRepository;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.domain.util.TestSecurityContextInjector;
import site.codemonster.comon.domain.util.TestUtil;
import site.codemonster.comon.global.error.ErrorCode;
import site.codemonster.comon.domain.alarm.entity.Alarm;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AlarmControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

        mockMvc.perform(get("/api/alarms")
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data.content[0].title").value("두 번째 알람"))
                .andExpect(jsonPath("$.data.content[0].content").value("두 번째 내용"))
                .andExpect(jsonPath("$.data.content[1].title").value("첫 번째 알람"))
                .andExpect(jsonPath("$.data.content[1].content").value("첫 번째 내용"))
                .andExpect(jsonPath("$.data.page.number").value(0))
                .andExpect(jsonPath("$.data.page.size").value(5))
                .andExpect(jsonPath("$.data.page.totalElements").value(2));
    }

    @Test
    @DisplayName("알람 목록 조회 실패 - 인증되지 않은 사용자")
    void getAlarmsFailWhenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/alarms")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED_MEMBER_ERROR.getMessage()));
    }
}
