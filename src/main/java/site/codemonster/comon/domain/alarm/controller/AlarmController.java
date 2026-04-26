package site.codemonster.comon.domain.alarm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.codemonster.comon.domain.alarm.dto.AlarmResponse;
import site.codemonster.comon.domain.alarm.service.AlarmService;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.global.error.dto.response.ApiResponse;
import site.codemonster.comon.global.log.annotation.Trace;

@Trace
@RestController
@RequestMapping({"/api/alarms", "/api/v1/alarms"})
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AlarmResponse>>> getAlarms(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @AuthenticationPrincipal Member member
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<AlarmResponse> alarms = alarmService.getAlarms(member, pageable);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithData(alarms));
    }
}
