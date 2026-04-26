package site.codemonster.comon.domain.alarm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.alarm.entity.Alarm;
import site.codemonster.comon.domain.alarm.repository.AlarmRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AlarmLowService {

    private final AlarmRepository alarmRepository;

    @Transactional(readOnly = true)
    public List<Alarm> findByMemberId(Long memberId) {
        return alarmRepository.findByMemberIdOrderByIdDesc(memberId);
    }


    public Alarm save(Alarm alarm) {
        return alarmRepository.save(alarm);
    }

    public void deleteByMemberId(Long memberId) {
        alarmRepository.deleteByMemberId(memberId);
    }
}
