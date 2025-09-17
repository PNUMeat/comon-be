package site.codemonster.comon.global.images.migration;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.repository.TeamRepository;
import site.codemonster.comon.global.images.enums.ImageConstant;

import java.util.List;

@Component
@RequiredArgsConstructor
@Order(4)
public class TeamImageMigration extends BaseImageMigration<Team> {

    private final TeamRepository teamRepository;

    @Override
    protected String getEntityType() {
        return "팀";
    }

    @Override
    protected List<Team> getAllEntities() {
        return teamRepository.findAll();
    }

    @Override
    protected String getCurrentImageUrl(Team entity) {
        return entity.getTeamIconUrl();
    }

    @Override
    protected Object getEntityId(Team entity) {
        return entity.getTeamId();
    }

    @Override
    protected void updateImageUrl(Team entity, String objectKey) {
        entity.updateTeamIconUrl(objectKey);
    }

    @Override
    protected boolean hasDefaultImage() {
        return true;
    }

    @Override
    protected boolean isDefaultImage(String imageUrl) {
        if (imageUrl == null) {
            return true;
        }
        return imageUrl.contains("/team/default-image.png") ||
                imageUrl.equals("team/default-image.png") ||
                imageUrl.equals(ImageConstant.DEFAULT_TEAM.getObjectKey());
    }

    @Override
    protected void updateToDefaultObjectKey(Team entity) {
        entity.updateTeamIconUrl(ImageConstant.DEFAULT_TEAM.getObjectKey());
    }
}
