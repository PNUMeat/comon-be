package site.codemonster.comon.global.images.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruitImage;
import site.codemonster.comon.domain.teamRecruit.repository.TeamRecruitImageRepository;
import site.codemonster.comon.global.util.convertUtils.ImageFieldConvertUtils;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // ArticleImage 마이그레이션 이후에 실행
public class TeamRecruitImageUrlMigrationRunner implements CommandLineRunner {

    private final TeamRecruitImageRepository teamRecruitImageRepository;
    private final ImageFieldConvertUtils imageFieldConvertUtils;

    @Transactional
    @Override
    public void run(String... args) {
        log.info("🚀 팀 모집글 이미지 URL 마이그레이션 시작");

        List<TeamRecruitImage> teamRecruitImages = teamRecruitImageRepository.findAll();
        log.info("📊 마이그레이션 대상 이미지 개수: {}", teamRecruitImages.size());

        int migratedCount = 0;
        int alreadyMigratedCount = 0;

        for (TeamRecruitImage teamRecruitImage : teamRecruitImages) {
            String currentImageUrl = teamRecruitImage.getImageUrl();

            if (imageFieldConvertUtils.isAlreadyObjectKey(currentImageUrl)) {
                alreadyMigratedCount++;
                log.debug("✅ 이미 객체 키 형태 - 이미지 ID: {}, URL: {}",
                        teamRecruitImage.getTeamRecruitImageId(), currentImageUrl);
                continue;
            }

            String objectKey = imageFieldConvertUtils.extractObjectKeyFromS3Url(currentImageUrl);
            if (objectKey != null) {
                teamRecruitImage.updateImageUrl(objectKey);
                migratedCount++;
                log.info("🔄 URL 마이그레이션 완료 - 이미지 ID: {}, {} -> {}",
                        teamRecruitImage.getTeamRecruitImageId(), currentImageUrl, objectKey);
            } else {
                log.warn("⚠️ S3 URL 패턴이 아님 - 이미지 ID: {}, URL: {}",
                        teamRecruitImage.getTeamRecruitImageId(), currentImageUrl);
            }
        }

        imageFieldConvertUtils.logMigrationStats("팀 모집글", teamRecruitImages.size(), migratedCount, alreadyMigratedCount);
    }
}
