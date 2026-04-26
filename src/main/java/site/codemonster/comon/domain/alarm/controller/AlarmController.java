package site.codemonster.comon.domain.alarm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.codemonster.comon.domain.alarm.dto.AlarmResponse;
import site.codemonster.comon.domain.alarm.service.AlarmService;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.global.error.dto.response.ApiResponse;
import site.codemonster.comon.global.log.annotation.Trace;

import java.util.List;

@Trace
@RestController
@RequestMapping({"/api/alarms", "/api/v1/alarms"})
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlarmResponse>>> getAlarms(
            @AuthenticationPrincipal Member member
    ) {
        List<AlarmResponse> alarms = alarmService.getAlarms(member);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithData(alarms));
    }
}
