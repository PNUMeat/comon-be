package site.codemonster.comon.domain.alarm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import site.codemonster.comon.domain.alarm.entity.Alarm;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    List<Alarm> findByMemberIdOrderByIdDesc(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Alarm al where al.member.id = :memberId")
    void deleteByMemberId(Long memberId);
}
