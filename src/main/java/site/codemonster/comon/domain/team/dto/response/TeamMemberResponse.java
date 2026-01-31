package site.codemonster.comon.domain.team.dto.response;

import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import site.codemonster.comon.global.util.s3.S3ImageUtil;

public record TeamMemberResponse(String uuid,
                                 String memberName,
                                 String memberExplain,
                                 String imageUrl,
                                 @JsonFormat(pattern = "yyyy-MM-dd")
                                 LocalDateTime registerDate,
                                 boolean isTeamManager
                                 ) {
    public TeamMemberResponse(TeamMember teamMember) {
        this(
                teamMember.getMember().getUuid(),
                teamMember.getMember().getMemberName(),
                teamMember.getMember().getDescription(),
                teamMember.getMember().getImageUrl(),
                teamMember.getCreatedDate(),
                teamMember.getIsTeamManager()
        );
    }
}
