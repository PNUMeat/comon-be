package site.codemonster.comon.domain.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.admin.dto.AdminLoginRequest;
import site.codemonster.comon.domain.admin.entity.AdminMember;
import site.codemonster.comon.domain.admin.repository.AdminMemberRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final AdminMemberRepository adminMemberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 관리자 로그인 검증
     */
    public AdminMember authenticateAdmin(AdminLoginRequest request) {
        AdminMember adminMember = adminMemberRepository
                .findByAdminIdAndIsActiveTrue(request.getAdminId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자 ID입니다."));

        // 임시로 평문 비교로 변경
        if (!request.getPassword().equals(adminMember.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 원래 BCrypt 비교 코드는 주석처리
        // if (!passwordEncoder.matches(request.getPassword(), adminMember.getPassword())) {
        //     throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        // }

        log.info("관리자 로그인 성공 - ID: {}, 이름: {}", adminMember.getAdminId(), adminMember.getName());
        return adminMember;
    }

    /**
     * 관리자 정보 조회 (ID로)
     */
    public AdminMember getAdminById(String adminId) {
        return adminMemberRepository
                .findByAdminIdAndIsActiveTrue(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));
    }

    /**
     * 관리자 정보 조회 (PK로)
     */
    public AdminMember getAdminByPk(Long id) {
        return adminMemberRepository
                .findById(id)
                .filter(AdminMember::isActive)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));
    }

    /**
     * 관리자 존재 여부 확인
     */
    public boolean existsByAdminId(String adminId) {
        return adminMemberRepository.existsByAdminId(adminId);
    }

    /**
     * 관리자 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long adminId, String currentPassword, String newPassword) {
        AdminMember adminMember = getAdminByPk(adminId);

        if (!passwordEncoder.matches(currentPassword, adminMember.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        adminMember.updatePassword(encodedPassword);

        log.info("관리자 비밀번호 변경 - ID: {}", adminMember.getAdminId());
    }

    /**
     * 관리자 계정 비활성화
     */
    @Transactional
    public void deactivateAdmin(Long adminId) {
        AdminMember adminMember = getAdminByPk(adminId);
        adminMember.deactivate();

        log.info("관리자 계정 비활성화 - ID: {}", adminMember.getAdminId());
    }

    /**
     * 관리자 계정 활성화
     */
    @Transactional
    public void activateAdmin(Long adminId) {
        AdminMember adminMember = adminMemberRepository
                .findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        adminMember.activate();

        log.info("관리자 계정 활성화 - ID: {}", adminMember.getAdminId());
    }
}
