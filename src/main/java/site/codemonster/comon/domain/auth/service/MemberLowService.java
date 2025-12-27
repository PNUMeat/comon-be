package site.codemonster.comon.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.global.error.Member.MemberNotFoundException;

@RequiredArgsConstructor
@Transactional
@Service
public class MemberLowService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Member getMemberByUUID(String uuid){
        return memberRepository.findByUuid(uuid)
                .orElseThrow(MemberNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Member getMemberById(Long memberId){
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    public void deleteById(Long Id) {
        memberRepository.deleteById(Id);
    }
}
