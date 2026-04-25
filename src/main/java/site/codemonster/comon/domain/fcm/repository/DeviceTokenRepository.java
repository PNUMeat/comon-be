package site.codemonster.comon.domain.fcm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import site.codemonster.comon.domain.fcm.entity.DeviceToken;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from DeviceToken dt where dt.member.id = :userId")
    void deleteByMemberId(Long memberId);

    List<DeviceToken> findByMemberId(Long memberId);

    @Query("select dt from DeviceToken dt where dt.member.id = :memberId and dt.token = :token")
    Optional<DeviceToken> findByMemberIdAndToken(Long memberId, String token);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from DeviceToken dt where dt.token = :token")
    void deleteByToken(String token);
}
