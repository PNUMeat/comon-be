package site.codemonster.comon.global.images.migration;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruitImage;
import site.codemonster.comon.domain.teamRecruit.repository.TeamRecruitImageRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Order(2)
public class TeamRecruitImageMigration extends BaseImageMigration<TeamRecruitImage> {

    private final TeamRecruitImageRepository teamRecruitImageRepository;

    @Override
    protected String getEntityType() {
        return "팀 모집글";
    }

    @Override
    protected List<TeamRecruitImage> getAllEntities() {
        return teamRecruitImageRepository.findAll();
    }

    @Override
    protected String getCurrentImageUrl(TeamRecruitImage entity) {
        return entity.getImageUrl();
    }

    @Override
    protected Object getEntityId(TeamRecruitImage entity) {
        return entity.getTeamRecruitImageId();
    }

    @Override
    protected void updateImageUrl(TeamRecruitImage entity, String objectKey) {
        entity.updateImageUrl(objectKey);
    }
}
