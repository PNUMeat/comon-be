package site.codemonster.comon.domain.teamRecruit.dto.response;

public record TeamRecruitUpdateResponse(
        Long recruitId
) {
    public static TeamRecruitUpdateResponse of(Long recruitId){
        return new TeamRecruitUpdateResponse(recruitId);
    }
}
