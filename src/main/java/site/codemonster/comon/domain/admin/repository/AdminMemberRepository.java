package site.codemonster.comon.domain.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.codemonster.comon.domain.admin.entity.AdminMember;

import java.util.Optional;

@Repository
public interface AdminMemberRepository extends JpaRepository<AdminMember, Long> {

    Optional<AdminMember> findByAdminIdAndIsActiveTrue(String adminId);

    boolean existsByAdminId(String adminId);
}
