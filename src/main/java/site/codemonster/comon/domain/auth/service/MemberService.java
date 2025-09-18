package site.codemonster.comon.domain.auth.service;

import site.codemonster.comon.domain.article.repository.ArticleImageRepository;
import site.codemonster.comon.domain.article.repository.ArticleRepository;
import site.codemonster.comon.domain.auth.dto.request.MemberProfileCreateRequest;
import site.codemonster.comon.domain.auth.dto.request.MemberProfileUpdateRequest;
import site.codemonster.comon.domain.auth.dto.response.MemberInfoResponse;
import site.codemonster.comon.domain.auth.dto.response.MemberProfileResponse;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.domain.auth.repository.RefreshTokenRepository;
import site.codemonster.comon.domain.team.dto.response.TeamAbstractResponse;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.repository.TeamRepository;
import site.codemonster.comon.domain.teamApply.repository.TeamApplyRepository;
import site.codemonster.comon.domain.teamMember.repository.TeamMemberRepository;
import site.codemonster.comon.domain.teamRecruit.repository.TeamRecruitImageRepository;
import site.codemonster.comon.domain.teamRecruit.repository.TeamRecruitRepository;
import site.codemonster.comon.global.error.Member.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import site.codemonster.comon.global.util.convertUtils.ImageFieldConvertUtils;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final ArticleRepository articleRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ArticleImageRepository articleImageRepository;
    private final TeamRecruitRepository teamRecruitRepository;
    private final TeamApplyRepository teamApplyRepository;
    private final TeamRecruitImageRepository teamRecruitImageRepository;
    private final ImageFieldConvertUtils imageFieldConvertUtils;

    @Transactional
    public void createMemberProfile(
        MemberProfileCreateRequest memberProfileCreateRequest,
        Member member
    ){
        member.updateProfile(
            memberProfileCreateRequest.memberName(), memberProfileCreateRequest.memberExplain(),
                imageFieldConvertUtils.convertImageUrlToObjectKey(memberProfileCreateRequest.imageUrl())
        );
    }

    @Transactional
    public Member updateMemberProfile(
        MemberProfileUpdateRequest memberProfileUpdateRequest,
        Member member
    ){
        member.updateProfile(
            memberProfileUpdateRequest.memberName(), memberProfileUpdateRequest.memberExplain(),
                imageFieldConvertUtils.convertImageUrlToObjectKey(memberProfileUpdateRequest.imageUrl())
        );
        return member;
    }

    public Member getMemberByUUID(String uuid){
        return memberRepository.findByUuid(uuid)
                .orElseThrow(MemberNotFoundException::new);
    }

    public Member getMemberById(Long memberId){
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    @Transactional
    public void deleteMember(Long memberId) {
        List<Long> teamRecruitIds = teamRecruitRepository.findIdsByMemberId(memberId);
        teamRecruitImageRepository.deleteByTeamRecruitIds(teamRecruitIds);
        teamApplyRepository.deleteTeamAppliesByTeamRecruitIds(teamRecruitIds);
        teamRecruitRepository.deleteByMemberId(memberId);
        teamApplyRepository.deleteTeamAppliesByMemberId(memberId);

        List<Team> teamsManagedByMember = teamRepository.findByTeamManagerId(memberId);
        for (Team team : teamsManagedByMember) {
            Long teamId = team.getTeamId();

            articleImageRepository.deleteByTeamTeamId(teamId);

            articleRepository.deleteByTeamTeamId(teamId);

            teamMemberRepository.deleteByTeamTeamId(teamId);

            teamRepository.deleteById(teamId);
        }

        articleImageRepository.deleteByMemberId(memberId);

        articleRepository.deleteByMemberId(memberId);

        teamMemberRepository.deleteByMemberId(memberId);

        refreshTokenRepository.deleteByUserId(memberId);

        memberRepository.deleteById(memberId);
    }
}
