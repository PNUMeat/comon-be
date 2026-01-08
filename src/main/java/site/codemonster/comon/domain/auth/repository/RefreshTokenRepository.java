package site.codemonster.comon.domain.auth.repository;


import site.codemonster.comon.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Boolean existsByToken(String Token);

    Optional<RefreshToken> findByToken(String refreshToken);

    Optional<RefreshToken> findRefreshTokenByMemberId(Long memberId);

    @Transactional
    void deleteByToken(String Token);

    @Modifying(clearAutomatically = true,  flushAutomatically = true)
    @Query("DELETE FROM RefreshToken rt WHERE rt.member.id = :memberId")
    void deleteByUserId(@Param("memberId") Long memberId);

}
