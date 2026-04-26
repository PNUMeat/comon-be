package site.codemonster.comon.domain.alarm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.alarm.entity.Alarm;
import site.codemonster.comon.domain.alarm.repository.AlarmRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class AlarmLowService {

    private final AlarmRepository alarmRepository;

    @Transactional(readOnly = true)
    public Page<Alarm> findByMemberIdWithPage(Long memberId, Pageable pageable) {
        return alarmRepository.findByMemberIdWithPage(memberId, pageable);
    }


    public Alarm save(Alarm alarm) {
        return alarmRepository.save(alarm);
    }

    public void deleteByMemberId(Long memberId) {
        alarmRepository.deleteByMemberId(memberId);
    }
}
