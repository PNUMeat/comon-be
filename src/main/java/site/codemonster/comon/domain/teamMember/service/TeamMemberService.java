package site.codemonster.comon.domain.teamMember.service;

import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.repository.ArticleImageRepository;
import site.codemonster.comon.domain.article.repository.ArticleRepository;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.service.MemberService;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.repository.TeamRepository;
import site.codemonster.comon.domain.teamApply.service.TeamApplyService;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamMember.repository.TeamMemberRepository;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import site.codemonster.comon.domain.teamRecruit.service.TeamRecruitService;
import site.codemonster.comon.global.error.Team.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final ArticleRepository articleRepository;
    private final TeamRecruitService teamRecruitService;
    private final ArticleImageRepository articleImageRepository;
    private final MemberService memberService;
    private final TeamRepository teamRepository;
    private final TeamApplyService teamApplyService;

    public List<TeamMember> getTeamMembersByMember(Member member){
        return teamMemberRepository.findByMemberIdOrderByTeamInfoIdDesc(member.getId());
    }

    @Transactional
    public TeamMember saveTeamMember(Team team, Member member, Boolean isTeamManager){
        TeamMember teamMember = new TeamMember(team, member, isTeamManager);

        return teamMemberRepository.save(teamMember);
    }

    @Transactional
    public void removeTeamMember(Member member, Long teamId){
        if(!existsByTeamIdAndMemberId(teamId, member)){
            throw new TeamMemberInvalidException();
        }
        teamMemberRepository.deleteByTeamTeamIdAndMember(teamId, member);

        // 내가 쓴 팀 모집글 삭제
        deleteTeamRecruit(teamId, member);

        // 내가 쓴 글의 이미지 삭제
        List<Article> findArticle = articleRepository.findArticleByTeamTeamIdAndMemberId(teamId, member.getId());

        List<Long> articleIds = findArticle.stream()
                .map(Article::getArticleId)
                .toList();

        articleImageRepository.deleteArticleImagesInArticleIds(articleIds);

        // 내가 쓴 글 삭제
        articleRepository.deleteByMemberIdAndTeamId(member.getId(), teamId);
    }

    public List<TeamMember> getTeamMemberAndTeamByMember(Member member){
        return teamMemberRepository.findTeamMemberByMemberIdWithTeamDesc(member.getId());
    }

    public boolean existsByTeamIdAndMemberId(Long teamId, Member member){
        return teamMemberRepository.existsByTeamTeamIdAndMemberId(teamId, member.getId());
    }

    @Transactional
    public void deleteTeamMemberByTeamId(Long teamId){
        teamMemberRepository.deleteByTeamTeamId(teamId);
    }

    public boolean checkMemberIsTeamManager(Long teamId, Member member){
        Optional<TeamMember> teamMemberOptional = teamMemberRepository.findTeamMemberByTeamTeamIdAndMemberId(teamId, member.getId());

        if(teamMemberOptional.isEmpty()){
            return false;
        }

        return teamMemberOptional.get().getIsTeamManager();
    }

    @Transactional
    public Boolean addTeamManager(Long teamId, Member manager, String memberUUID){
        if(!checkMemberIsTeamManager(teamId, manager)){
            throw new TeamManagerInvalidException();
        }

        Member notManager = memberService.getMemberByUUID(memberUUID);

        TeamMember teamMemberOfNotManager = getTeamMemberByTeamIdAndMemberId(teamId, notManager);

        if(teamMemberOfNotManager.getIsTeamManager()){
            throw new AlreadyTeamManagerException();
        }

        return teamMemberOfNotManager.updateTeamManagerStatus(true);
    }

    @Transactional
    public Boolean transferTeamManager(Long teamId, Member manager, String memberUUID){
        TeamMember teamMemberOfManager = getTeamMemberByTeamIdAndMemberId(teamId, manager);

        if(!teamMemberOfManager.getIsTeamManager()){
            throw new TeamManagerInvalidException();
        }

        Member notManager = memberService.getMemberByUUID(memberUUID);

        TeamMember teamMemberOfNotManager = getTeamMemberByTeamIdAndMemberId(teamId, notManager);

        if(teamMemberOfNotManager.getIsTeamManager()){
            throw new AlreadyTeamManagerException();
        }

        deleteTeamRecruit(teamId, manager);

        teamMemberOfManager.updateTeamManagerStatus(false);
        return teamMemberOfNotManager.updateTeamManagerStatus(true);
    }

    @Transactional
    public Boolean demoteTeamManager(Long teamId, Member manager, String memberUUID){
        if(!checkMemberIsTeamManager(teamId, manager)){
            throw new TeamManagerInvalidException();
        }

        Member notManager = memberService.getMemberByUUID(memberUUID);

        TeamMember teamMemberOfNotManager = getTeamMemberByTeamIdAndMemberId(teamId, notManager);

        if(!teamMemberOfNotManager.getIsTeamManager()){
            throw new TeamManagerInvalidException();
        }

        deleteTeamRecruit(teamId, notManager);

        return teamMemberOfNotManager.updateTeamManagerStatus(false);
    }

    @Transactional
    public void removeMemberByManger(Long teamId, Member manager, String memberUUID){
        if(!checkMemberIsTeamManager(teamId, manager)){
            throw new TeamManagerInvalidException();
        }

        Member removedMember = memberService.getMemberByUUID(memberUUID);

        removeTeamMember(removedMember, teamId);
    }

    public TeamMember getTeamMemberByTeamIdAndMemberId(Long teamId, Member member) {
        return teamMemberRepository.findTeamMemberByTeamTeamIdAndMemberId(teamId, member.getId())
                .orElseThrow(TeamMemberInvalidException::new);
    }

    public List<TeamMember> getTeamMembersByTeamId(Long teamId, Member manager) {
        if(!checkMemberIsTeamManager(teamId, manager)){
            throw new TeamManagerInvalidException();
        }

        return teamMemberRepository.findByTeamId(teamId);
    }

    public boolean checkMemberIsTeamManagerOrThrow(TeamMember teamMember){
        if(!teamMember.getIsTeamManager()){
            throw new TeamManagerInvalidException();
        }

        return true;
    }

    public void throwIfMemberAlreadyInTeam(Long teamId, Member member){
        if(teamMemberRepository.existsByTeamTeamIdAndMemberId(teamId, member.getId())){
            throw new TeamAlreadyJoinException();
        }
    }

    @Transactional
    public void deleteTeamRecruit(Long teamId, Member member) {
        Team team = teamRepository.findTeamByTeamIdWithTeamRecruit(teamId)
                .orElseThrow(TeamNotFoundException::new);

        TeamRecruit teamRecruit = team.getTeamRecruit();
        if(teamRecruit != null && team.getTeamRecruit().isTeamRecruitOwner(member)){
            teamRecruitService.forceDeleteTeamRecruit(teamRecruit);
        }
    }

    @Transactional
    public void inviteTeamMember(Team team, List<Member> members, TeamRecruit teamRecruit){
        for (Member member : members) {
            if(existsByTeamIdAndMemberId(team.getTeamId(), member)){
                throw new TeamAlreadyJoinException();
            }

            saveTeamMember(team, member, false);
        }

        teamApplyService.deleteTeamApplyAfterTeamMake(teamRecruit);
    }
}
