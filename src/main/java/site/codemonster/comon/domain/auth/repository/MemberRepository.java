package site.codemonster.comon.domain.auth.repository;


import site.codemonster.comon.domain.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberUniqueId(String memberUniqueId);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByUuid(String uuid);
}
