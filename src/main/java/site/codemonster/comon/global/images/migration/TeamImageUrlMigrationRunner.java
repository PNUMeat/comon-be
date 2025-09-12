package site.codemonster.comon.global.images.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.repository.TeamRepository;
import site.codemonster.comon.global.images.enums.ImageConstant;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(4) // Member 이미지 마이그레이션 이후에 실행
public class TeamImageUrlMigrationRunner implements CommandLineRunner {

    private final TeamRepository teamRepository;
    private final ImageMigrationUtils imageFieldConvertUtils;

    @Transactional
    @Override
    public void run(String... args) {
        log.info("🚀 팀 이미지 URL 마이그레이션 시작");

        List<Team> teams = teamRepository.findAll();
        log.info("📊 마이그레이션 대상 팀 수: {}", teams.size());

        int migratedCount = 0;
        int alreadyMigratedCount = 0;
        int defaultImageCount = 0;

        for (Team team : teams) {
            String currentImageUrl = team.getTeamIconUrl();

            // 기본 이미지인 경우 객체 키로 변경
            if (isDefaultImage(currentImageUrl)) {
                team.updateTeamIconUrl(ImageConstant.DEFAULT_TEAM.getObjectKey());
                defaultImageCount++;
                log.debug("🔄 기본 이미지를 객체 키로 변경 - 팀 ID: {}", team.getTeamId());
                continue;
            }

            if (imageFieldConvertUtils.isAlreadyObjectKey(currentImageUrl)) {
                alreadyMigratedCount++;
                log.debug("✅ 이미 객체 키 형태 - 팀 ID: {}, URL: {}",
                        team.getTeamId(), currentImageUrl);
                continue;
            }

            String objectKey = imageFieldConvertUtils.extractObjectKeyFromS3Url(currentImageUrl);
            if (objectKey != null) {
                team.updateTeamIconUrl(objectKey);
                migratedCount++;
                log.info("🔄 URL 마이그레이션 완료 - 팀 ID: {}, {} -> {}",
                        team.getTeamId(), currentImageUrl, objectKey);
            } else {
                log.warn("⚠️ S3 URL 패턴이 아님 - 팀 ID: {}, URL: {}",
                        team.getTeamId(), currentImageUrl);
            }
        }

        log.info("🎉 팀 이미지 URL 마이그레이션 완료");
        log.info("📈 마이그레이션 통계 - S3 URL 변경: {}개, 기본 이미지 변경: {}개, 이미 완료: {}개, 전체: {}개",
                migratedCount, defaultImageCount, alreadyMigratedCount, teams.size());
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
        return imageUrl.contains("/team/default-image.png") ||
                imageUrl.equals("team/default-image.png") ||
                imageUrl.equals(ImageConstant.DEFAULT_TEAM.getObjectKey());
    }
}
