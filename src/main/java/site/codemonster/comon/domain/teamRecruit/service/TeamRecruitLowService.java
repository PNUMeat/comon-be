package site.codemonster.comon.domain.teamRecruit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.teamApply.repository.TeamApplyRepository;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import site.codemonster.comon.domain.teamRecruit.repository.TeamRecruitImageRepository;
import site.codemonster.comon.domain.teamRecruit.repository.TeamRecruitRepository;
import site.codemonster.comon.global.error.TeamRecruit.TeamRecruitNotAuthorException;
import site.codemonster.comon.global.error.TeamRecruit.TeamRecruitNotFoundException;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TeamRecruitLowService {

    private final TeamRecruitRepository teamRecruitRepository;
    private final TeamRecruitImageRepository teamRecruitImageRepository;
    private final TeamApplyRepository teamApplyRepository;


    public void forceDeleteTeamRecruit(Long teamRecruitId) {
        teamRecruitImageRepository.deleteTeamRecruitImagesByTeamRecruitId(teamRecruitId);
        teamApplyRepository.deleteTeamAppliesByTeamRecruitId(teamRecruitId);
        teamRecruitRepository.deleteById(teamRecruitId);
    }

    @Transactional(readOnly = true)
    public Page<TeamRecruit> getTeamRecruitmentsUsingPaging(Pageable pageable, String status){
        return switch (status) {
            case "open" -> teamRecruitRepository.findByIsRecruitingTrueWithPaging(pageable);
            case "closed" -> teamRecruitRepository.findByIsRecruitingFalseWithPaging(pageable);
            default -> teamRecruitRepository.findAllWithPaging(pageable);
        };
    }

    @Transactional(readOnly = true)
    public TeamRecruit findByTeamRecruitIdOrThrow(Long teamRecruitId){
        return teamRecruitRepository.findById(teamRecruitId)
                .orElseThrow(TeamRecruitNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public TeamRecruit findByTeamRecruitIdWithMemberOrThrow(Long teamRecruitId){
        return teamRecruitRepository.findTeamRecruitByTeamRecruitIdWithRelation(teamRecruitId)
                .orElseThrow(TeamRecruitNotFoundException::new);
    }

    public void deleteTeamRecruit(TeamRecruit teamRecruit, Member member){
        if(!teamRecruit.isAuthor(member)){
            throw new TeamRecruitNotAuthorException();
        }

        teamRecruitImageRepository.deleteTeamRecruitImagesByTeamRecruitId(teamRecruit.getTeamRecruitId());
        teamApplyRepository.deleteTeamAppliesByTeamRecruitId(teamRecruit.getTeamRecruitId());
        teamRecruitRepository.delete(teamRecruit);
    }

    public TeamRecruit save(TeamRecruit teamRecruit){
        return teamRecruitRepository.save(teamRecruit);
    }

    @Transactional(readOnly = true)
    public void isAuthorOrThrow(TeamRecruit teamRecruit, Member member){
        if(!teamRecruit.isAuthor(member)){
            throw new TeamRecruitNotAuthorException();
        }
    }

    @Transactional(readOnly = true)
    public List<Long> findIdsByMemberId(Long memberId){
        return teamRecruitRepository.findIdsByMemberId(memberId);
    }

    public void deleteByMemberId(Long memberId){
        teamRecruitRepository.deleteByMemberId(memberId);
    }
}
