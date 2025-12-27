package site.codemonster.comon.domain.teamRecruit.service;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.teamApply.repository.TeamApplyRepository;
import site.codemonster.comon.domain.teamRecruit.dto.request.TeamRecruitCreateRequest;
import site.codemonster.comon.domain.teamRecruit.dto.request.TeamRecruitUpdateRequest;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import site.codemonster.comon.domain.teamRecruit.repository.TeamRecruitImageRepository;
import site.codemonster.comon.domain.teamRecruit.repository.TeamRecruitRepository;
import site.codemonster.comon.global.error.TeamRecruit.TeamRecruitDuplicateException;
import site.codemonster.comon.global.error.TeamRecruit.TeamRecruitNotAuthorException;
import site.codemonster.comon.global.error.TeamRecruit.TeamRecruitNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRecruitService {

    private final TeamRecruitRepository teamRecruitRepository;
    private final TeamRecruitImageRepository teamRecruitImageRepository;
    private final TeamApplyRepository teamApplyRepository;

    @Transactional
    public TeamRecruit createTeamRecruit(TeamRecruitCreateRequest teamRecruitCreateRequest, Optional<Team> team, Member member){


        if (team.isPresent() && team.get().getTeamRecruit() != null) throw new TeamRecruitDuplicateException();

        TeamRecruit teamRecruit = new TeamRecruit(team.orElse(null), member, teamRecruitCreateRequest.teamRecruitTitle(), teamRecruitCreateRequest.teamRecruitBody(), teamRecruitCreateRequest.chatUrl());

        return teamRecruitRepository.save(teamRecruit);
    }

    public Page<TeamRecruit> getTeamRecruitmentsUsingPaging(Pageable pageable, String status){
        return switch (status) {
            case "open" -> teamRecruitRepository.findByIsRecruitingTrueWithPaging(pageable);
            case "closed" -> teamRecruitRepository.findByIsRecruitingFalseWithPaging(pageable);
            default -> teamRecruitRepository.findAllWithPaging(pageable);
        };
    }

    public TeamRecruit findByTeamRecruitIdOrThrow(Long teamRecruitId){
        return teamRecruitRepository.findById(teamRecruitId)
                .orElseThrow(TeamRecruitNotFoundException::new);
    }

    public TeamRecruit findByTeamRecruitIdWithMemberOrThrow(Long teamRecruitId){
        return teamRecruitRepository.findTeamRecruitByTeamRecruitIdWithRelation(teamRecruitId)
                .orElseThrow(TeamRecruitNotFoundException::new);
    }

    @Transactional
    public void changeTeamRecruitStatus(Long teamRecruitId, Member member){
        TeamRecruit teamRecruit = findByTeamRecruitIdWithMemberOrThrow(teamRecruitId);

        if(!teamRecruit.isAuthor(member)){
            throw new TeamRecruitNotAuthorException();
        }

        if(teamRecruit.isRecruiting()){
            teamApplyRepository.deleteTeamAppliesByTeamRecruitId(teamRecruitId);
        }

        teamRecruit.changeRecruitingStatus();
    }

    @Transactional
    public void updateTeamRecruit(Long teamRecruitId, Member member, TeamRecruitUpdateRequest teamRecruitUpdateRequest){
        TeamRecruit teamRecruit = findByTeamRecruitIdWithMemberOrThrow(teamRecruitId);

        if(!teamRecruit.isAuthor(member)){
            throw new TeamRecruitNotAuthorException();
        }

        teamRecruit.updateTeamRecruit(teamRecruitUpdateRequest);
    }

    @Transactional
    public void deleteTeamRecruit(TeamRecruit teamRecruit, Member member){
        if(!teamRecruit.isAuthor(member)){
            throw new TeamRecruitNotAuthorException();
        }

        teamRecruitImageRepository.deleteTeamRecruitImagesByTeamRecruitId(teamRecruit.getTeamRecruitId());
        teamApplyRepository.deleteTeamAppliesByTeamRecruitId(teamRecruit.getTeamRecruitId());
        teamRecruitRepository.delete(teamRecruit);
    }

    @Transactional
    public void forceDeleteTeamRecruit(TeamRecruit teamRecruit) {
        teamRecruitImageRepository.deleteTeamRecruitImagesByTeamRecruitId(teamRecruit.getTeamRecruitId());
        teamApplyRepository.deleteTeamAppliesByTeamRecruitId(teamRecruit.getTeamRecruitId());
        teamRecruitRepository.delete(teamRecruit);
    }

    public void isAuthorOrThrow(TeamRecruit teamRecruit, Member member){
        if(!teamRecruit.isAuthor(member)){
            throw new TeamRecruitNotAuthorException();
        }
    }
}
