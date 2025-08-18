package site.codemonster.comon.domain.adminAuth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.codemonster.comon.domain.adminAuth.entity.AdminMember;

import java.util.Optional;

@Repository
public interface AdminMemberRepository extends JpaRepository<AdminMember, Long> {
    Optional<AdminMember> findByAdminId(String adminId);
}
