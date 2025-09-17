package site.codemonster.comon.global.images.migration;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.global.images.enums.ImageConstant;

import java.util.List;

@Component
@RequiredArgsConstructor
@Order(3)
public class MemberImageMigration extends BaseImageMigration<Member> {

    private final MemberRepository memberRepository;

    @Override
    protected String getEntityType() {
        return "회원 프로필";
    }

    @Override
    protected List<Member> getAllEntities() {
        return memberRepository.findAll();
    }

    @Override
    protected String getCurrentImageUrl(Member entity) {
        return entity.getImageUrl();
    }

    @Override
    protected Object getEntityId(Member entity) {
        return entity.getId();
    }

    @Override
    protected void updateImageUrl(Member entity, String objectKey) {
        entity.updateImageUrl(objectKey);
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
        return imageUrl.contains("/profile/default-image.png") ||
                imageUrl.equals("profile/default-image.png") ||
                imageUrl.equals(ImageConstant.DEFAULT_MEMBER_PROFILE.getObjectKey());
    }

    @Override
    protected void updateToDefaultObjectKey(Member entity) {
        entity.updateImageUrl(ImageConstant.DEFAULT_MEMBER_PROFILE.getObjectKey());
    }
}
