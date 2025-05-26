package site.codemonster.comon.domain.team.dto.response;

import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record TeamMemberResponse(String uuid,
                                 String memberName,
                                 String memberExplain,
                                 String imageUrl,
                                 @JsonFormat(pattern = "yyyy-MM-dd")
                                 LocalDateTime registerDate,
                                 boolean isTeamManager
                                 ) {
    public static TeamMemberResponse of(TeamMember tm){
        return new TeamMemberResponse(tm.getMember().getUuid(),
                tm.getMember().getMemberName(),
                tm.getMember().getDescription(),
                tm.getMember().getImageUrl(),
                tm.getCreatedDate(),
                tm.getIsTeamManager());
    }
}
