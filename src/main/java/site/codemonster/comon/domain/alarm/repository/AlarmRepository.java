package site.codemonster.comon.domain.alarm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.codemonster.comon.domain.alarm.entity.Alarm;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    List<Alarm> findByMemberIdOrderByIdDesc(Long memberId);
}
