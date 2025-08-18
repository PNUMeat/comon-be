package site.codemonster.comon.domain.adminAuth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.adminAuth.dto.AdminLoginRequest;
import site.codemonster.comon.domain.adminAuth.entity.AdminMember;
import site.codemonster.comon.domain.adminAuth.repository.AdminMemberRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final AdminMemberRepository adminMemberRepository;

    /**
     * 관리자 로그인 검증 (평문 비밀번호)
     */
    public AdminMember authenticateAdmin(AdminLoginRequest request) {
        AdminMember adminMember = adminMemberRepository
                .findByAdminId(request.getAdminId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자 ID입니다."));

        if (!request.getPassword().equals(adminMember.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        log.info("관리자 로그인 성공 - ID: {}, 이름: {}", adminMember.getAdminId(), adminMember.getName());
        return adminMember;
    }

    /**
     * 관리자 정보 조회 (ID로)
     */
    public AdminMember getAdminById(String adminId) {
        return adminMemberRepository
                .findByAdminId(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));
    }

    /**
     * 관리자 정보 조회 (PK로)
     */
    public AdminMember getAdminByPk(Long id) {
        return adminMemberRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));
    }

    /**
     * 관리자 존재 여부 확인
     */
    public boolean existsByAdminId(String adminId) {
        return adminMemberRepository.existsByAdminId(adminId);
    }
}
