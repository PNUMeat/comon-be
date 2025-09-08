package site.codemonster.comon.domain.adminAuth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.adminAuth.dto.AdminLoginRequest;
import site.codemonster.comon.domain.adminAuth.entity.AdminMember;
import site.codemonster.comon.domain.adminAuth.repository.AdminMemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final AdminMemberRepository adminMemberRepository;

    public AdminMember authenticateAdmin(AdminLoginRequest request) {
        AdminMember adminMember = adminMemberRepository
                .findByAdminId(request.getAdminId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자 ID입니다."));

        if (!request.getPassword().equals(adminMember.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return adminMember;
    }
}
