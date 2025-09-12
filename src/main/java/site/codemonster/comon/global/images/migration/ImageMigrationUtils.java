package site.codemonster.comon.global.images.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ImageMigrationUtils {

    // S3 URL에서 객체 키를 추출하는 정규식 패턴
    private static final Pattern S3_URL_PATTERN = Pattern.compile(
            "https://[^/]+\\.s3\\.[^/]+\\.amazonaws\\.com/(.+)"
    );

    /**
     * S3 URL에서 객체 키를 추출합니다.
     *
     * @param s3Url S3 전체 URL
     * @return 객체 키 (예: "article/dee6026b-6fd6-4d1c-bd42-25894b3b846c.png")
     */
    public String extractObjectKeyFromS3Url(String s3Url) {
        if (s3Url == null || s3Url.trim().isEmpty()) {
            return null;
        }

        Matcher matcher = S3_URL_PATTERN.matcher(s3Url.trim());
        if (matcher.matches()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * 이미 객체 키 형태인지 확인합니다.
     * (https://로 시작하지 않으면 객체 키로 간주)
     */
    public boolean isAlreadyObjectKey(String imageUrl) {
        return imageUrl != null &&
                !imageUrl.trim().isEmpty() &&
                !imageUrl.startsWith("https://") &&
                !imageUrl.startsWith("http://");
    }

    /**
     * 마이그레이션 통계를 로깅합니다.
     */
    public void logMigrationStats(String entityType, int totalCount, int migratedCount, int alreadyMigratedCount) {
        log.info("🎉 {} 이미지 URL 마이그레이션 완료", entityType);
        log.info("📈 마이그레이션 통계 - 변경: {}개, 이미 완료: {}개, 전체: {}개",
                migratedCount, alreadyMigratedCount, totalCount);
    }

    /**
     * 확장된 마이그레이션 통계를 로깅합니다. (기본 이미지 변경 포함)
     */
    public void logMigrationStatsWithDefault(String entityType, int totalCount, int migratedCount,
                                             int defaultImageCount, int alreadyMigratedCount) {
        log.info("🎉 {} 이미지 URL 마이그레이션 완료", entityType);
        log.info("📈 마이그레이션 통계 - S3 URL 변경: {}개, 기본 이미지 변경: {}개, 이미 완료: {}개, 전체: {}개",
                migratedCount, defaultImageCount, alreadyMigratedCount, totalCount);
    }
}
