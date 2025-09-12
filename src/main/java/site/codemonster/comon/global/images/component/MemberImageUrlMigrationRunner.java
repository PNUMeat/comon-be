package site.codemonster.comon.global.images.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.global.images.enums.ImageConstant;
import site.codemonster.comon.global.util.convertUtils.ImageFieldConvertUtils;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(3) // 다른 이미지 마이그레이션 이후에 실행
public class MemberImageUrlMigrationRunner implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final ImageFieldConvertUtils imageFieldConvertUtils;

    @Transactional
    @Override
    public void run(String... args) {
        log.info("🚀 회원 프로필 이미지 URL 마이그레이션 시작");

        List<Member> members = memberRepository.findAll();
        log.info("📊 마이그레이션 대상 회원 수: {}", members.size());

        int migratedCount = 0;
        int alreadyMigratedCount = 0;
        int defaultImageCount = 0;

        for (Member member : members) {
            String currentImageUrl = member.getImageUrl();

            // 기본 이미지인 경우 객체 키로 변경
            if (isDefaultImage(currentImageUrl)) {
                member.updateImageUrl(ImageConstant.DEFAULT_MEMBER_PROFILE.getObjectKey());
                defaultImageCount++;
                log.debug("🔄 기본 이미지를 객체 키로 변경 - 회원 ID: {}", member.getId());
                continue;
            }

            if (imageFieldConvertUtils.isAlreadyObjectKey(currentImageUrl)) {
                alreadyMigratedCount++;
                log.debug("✅ 이미 객체 키 형태 - 회원 ID: {}, URL: {}",
                        member.getId(), currentImageUrl);
                continue;
            }

            String objectKey = imageFieldConvertUtils.extractObjectKeyFromS3Url(currentImageUrl);
            if (objectKey != null) {
                member.updateImageUrl(objectKey);
                migratedCount++;
                log.info("🔄 URL 마이그레이션 완료 - 회원 ID: {}, {} -> {}",
                        member.getId(), currentImageUrl, objectKey);
            } else {
                log.warn("⚠️ S3 URL 패턴이 아님 - 회원 ID: {}, URL: {}",
                        member.getId(), currentImageUrl);
            }
        }

        log.info("🎉 회원 프로필 이미지 URL 마이그레이션 완료");
        log.info("📈 마이그레이션 통계 - S3 URL 변경: {}개, 기본 이미지 변경: {}개, 이미 완료: {}개, 전체: {}개",
                migratedCount, defaultImageCount, alreadyMigratedCount, members.size());
    }

    /**
     * 기본 이미지 URL인지 확인합니다.
     * (구버전 기본 이미지 URL들도 포함)
     */
    private boolean isDefaultImage(String imageUrl) {
        if (imageUrl == null) {
            return true;
        }

        // 구버전 기본 이미지 URL들
        return imageUrl.contains("/profile/default-image.png") ||
                imageUrl.equals("profile/default-image.png") ||
                imageUrl.equals(ImageConstant.DEFAULT_MEMBER_PROFILE.getObjectKey());
    }
}
