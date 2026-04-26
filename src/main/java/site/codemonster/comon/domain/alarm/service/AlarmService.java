package site.codemonster.comon.domain.alarm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.alarm.dto.AlarmResponse;
import site.codemonster.comon.domain.alarm.entity.Alarm;
import site.codemonster.comon.domain.auth.entity.Member;

@Service
@Transactional
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmLowService alarmLowService;

    @Transactional(readOnly = true)
    public Page<AlarmResponse> getAlarms(Member member, Pageable pageable) {
        return alarmLowService.findByMemberIdWithPage(member.getId(), pageable)
                .map(AlarmResponse::new);
    }

    public Alarm save(Alarm alarm) {
        return alarmLowService.save(alarm);
    }
}
