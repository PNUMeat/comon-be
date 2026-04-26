package site.codemonster.comon.domain.alarm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.alarm.dto.AlarmResponse;
import site.codemonster.comon.domain.alarm.entity.Alarm;
import site.codemonster.comon.domain.auth.entity.Member;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmLowService alarmLowService;

    @Transactional(readOnly = true)
    public List<AlarmResponse> getAlarms(Member member) {
        return alarmLowService.findByMemberId(member.getId()).stream()
                .map(AlarmResponse::new)
                .toList();
    }

    public Alarm save(Alarm alarm) {
        return alarmLowService.save(alarm);
    }
}
