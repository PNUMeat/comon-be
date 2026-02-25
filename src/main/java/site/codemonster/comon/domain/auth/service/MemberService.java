package site.codemonster.comon.domain.auth.service;

import site.codemonster.comon.domain.article.service.ArticleImageLowService;
import site.codemonster.comon.domain.article.service.ArticleLowService;
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
import site.codemonster.comon.domain.team.service.TeamLowService;
import site.codemonster.comon.domain.teamApply.repository.TeamApplyRepository;
import site.codemonster.comon.domain.teamApply.service.TeamApplyLowService;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamMember.repository.TeamMemberRepository;
import site.codemonster.comon.domain.teamMember.service.TeamMemberLowService;
import site.codemonster.comon.domain.teamRecruit.repository.TeamRecruitImageRepository;
import site.codemonster.comon.domain.teamRecruit.repository.TeamRecruitRepository;
import site.codemonster.comon.domain.teamRecruit.service.TeamRecruitLowService;
import site.codemonster.comon.global.error.Member.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import site.codemonster.comon.global.util.s3.S3ImageUtil;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberLowService memberLowService;
    private final TeamLowService teamLowService;
    private final ArticleLowService articleLowService;
    private final TeamMemberLowService teamMemberLowService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ArticleImageLowService articleImageLowService;
    private final TeamRecruitLowService teamRecruitLowService;
    private final TeamApplyLowService teamApplyLowService;
    private final TeamRecruitImageRepository teamRecruitImageRepository;

    public void createMemberProfile(
        MemberProfileCreateRequest memberProfileCreateRequest,
        Member member
    ){
        member.updateProfile(
            memberProfileCreateRequest.memberName(), memberProfileCreateRequest.memberExplain(),
                S3ImageUtil.convertImageUrlToObjectKey(memberProfileCreateRequest.imageUrl())
        );
    }

    public Member updateMemberProfile(
        MemberProfileUpdateRequest memberProfileUpdateRequest,
        Member member
    ){
        member.updateProfile(
            memberProfileUpdateRequest.memberName(), memberProfileUpdateRequest.memberExplain(),
                S3ImageUtil.convertImageUrlToObjectKey(memberProfileUpdateRequest.imageUrl())
        );
        return member;
    }

    public MemberProfileResponse findProfileMemberInfoByUUID(String uuid){
        Member member = memberLowService.getMemberByUUID(uuid);

        return new MemberProfileResponse(member);
    }

    public void deleteMember(Long memberId) {
        List<Long> teamRecruitIds = teamRecruitLowService.findIdsByMemberId(memberId);
        teamRecruitImageRepository.deleteByTeamRecruitIds(teamRecruitIds);
        teamApplyLowService.deleteTeamAppliesByTeamRecruitIds(teamRecruitIds);
        teamRecruitLowService.deleteByMemberId(memberId);
        teamApplyLowService.deleteTeamAppliesByMemberId(memberId);

        List<Team> teamsManagedByMember = teamLowService.findByTeamManagerId(memberId);
        for (Team team : teamsManagedByMember) {
            Long teamId = team.getTeamId();

            articleLowService.deleteByTeamTeamId(teamId);

            teamMemberLowService.deleteByTeamTeamId(teamId);

            teamLowService.deleteById(teamId);
        }

        articleLowService.deleteByMemberId(memberId);

        teamMemberLowService.deleteByMemberId(memberId);

        refreshTokenRepository.deleteByUserId(memberId);

        memberLowService.deleteById(memberId);
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse findMyMemberInfo(Member member) {
        List<TeamMember> teamMemberAndTeamByMember = teamMemberLowService.getTeamMemberAndTeamByMember(member);

        List<TeamAbstractResponse> teamAbstractResponses = teamMemberAndTeamByMember.stream()
                .map(TeamMember::getTeam)
                .map(TeamAbstractResponse::of)
                .toList();

        return new MemberInfoResponse(member, teamAbstractResponses);
    }
}
