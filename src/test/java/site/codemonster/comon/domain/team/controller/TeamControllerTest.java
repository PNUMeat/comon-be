package site.codemonster.comon.domain.team.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.domain.team.dto.request.TeamCreateRequest;
import site.codemonster.comon.domain.team.dto.response.TeamCreateResponse;
import site.codemonster.comon.domain.team.enums.Topic;
import site.codemonster.comon.domain.util.TestSecurityContextInjector;
import site.codemonster.comon.domain.util.TestUtil;
import site.codemonster.comon.global.error.dto.response.ApiResponse;
import site.codemonster.comon.global.error.response.ErrorValidationResult;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.SoftAssertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;


    @ParameterizedTest
    @ValueSource(ints = {1,25,50})
    @DisplayName("팀 생성 성공")
    void teamCreateSuccess(int memberLimit) throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());

        TestSecurityContextInjector.inject(member);

        TeamCreateRequest teamCreateRequest = new TeamCreateRequest("팀이름", "팀설명", Topic.CODINGTEST.getName(), memberLimit, "1234", null, null, null);
        String requestBody = objectMapper.writeValueAsString(teamCreateRequest);


        String response = mockMvc.perform(post("/api/v1/teams")
                .with(securityContext(SecurityContextHolder.getContext()))
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);


        ApiResponse<TeamCreateResponse> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<TeamCreateResponse>>() {
        });

        TeamCreateResponse teamCreateResponse = apiResponse.getData();

        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
            softly.assertThat(teamCreateResponse.teamId()).isNotNull();
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {0,51})
    @DisplayName("팀 생성 실패 - 팀원 수는 1이상 50 이하")
    void teamCreateFail(int memberLimit) throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());

        TestSecurityContextInjector.inject(member);

        TeamCreateRequest teamCreateRequest = new TeamCreateRequest("팀이름", "팀설명", Topic.CODINGTEST.getName(), memberLimit, "1234", null, null, null);
        String requestBody = objectMapper.writeValueAsString(teamCreateRequest);


        String response = mockMvc.perform(post("/api/v1/teams")
                .with(securityContext(SecurityContextHolder.getContext()))
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);


        ApiResponse<Map<String,String>> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<Map<String,String>>>() {
        });


        Map<String, String> data = apiResponse.getData();

        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.FAIL);
            softly.assertThat(data.get("memberLimit")).isEqualTo("1 ~ 50 사이의 숫자를 입력해주세요.");
            softly.assertThat(apiResponse.getCode()).isEqualTo(ErrorValidationResult.ERROR_STATUS_CODE);
            softly.assertThat(apiResponse.getMessage()).isEqualTo("1 ~ 50 사이의 숫자를 입력해주세요.");
        });
    }

}
