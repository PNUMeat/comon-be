package site.codemonster.comon.domain.alarm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import site.codemonster.comon.domain.alarm.entity.Alarm;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    @Query("select al from Alarm al where al.member.id = :memberId")
    Page<Alarm> findByMemberIdWithPage(Long memberId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Alarm al where al.member.id = :memberId")
    void deleteByMemberId(Long memberId);
}
