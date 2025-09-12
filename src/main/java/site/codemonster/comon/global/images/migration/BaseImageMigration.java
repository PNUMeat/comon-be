package site.codemonster.comon.global.images.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class BaseImageMigration<T> implements CommandLineRunner {

    // S3 URL에서 객체 키를 추출하는 정규식 패턴
    private static final Pattern S3_URL_PATTERN = Pattern.compile(
            "https://[^/]+\\.s3\\.[^/]+\\.amazonaws\\.com/(.+)"
    );

    @Transactional
    @Override
    public void run(String... args) {
        String entityType = getEntityType();
        log.info("🚀 {} 이미지 URL 마이그레이션 시작", entityType);

        List<T> entities = getAllEntities();
        log.info("📊 마이그레이션 대상 {} 개수: {}", entityType, entities.size());

        int migratedCount = 0;
        int alreadyMigratedCount = 0;
        int defaultImageCount = 0;

        for (T entity : entities) {
            String currentImageUrl = getCurrentImageUrl(entity);

            // 기본 이미지 처리 (해당하는 경우만)
            if (hasDefaultImage() && isDefaultImage(currentImageUrl)) {
                updateToDefaultObjectKey(entity);
                defaultImageCount++;
                log.debug("🔄 기본 이미지를 객체 키로 변경 - {} ID: {}", entityType, getEntityId(entity));
                continue;
            }

            if (isAlreadyObjectKey(currentImageUrl)) {
                alreadyMigratedCount++;
                log.debug("✅ 이미 객체 키 형태 - {} ID: {}, URL: {}",
                        entityType, getEntityId(entity), currentImageUrl);
                continue;
            }

            String objectKey = extractObjectKeyFromS3Url(currentImageUrl);
            if (objectKey != null) {
                updateImageUrl(entity, objectKey);
                migratedCount++;
                log.info("🔄 URL 마이그레이션 완료 - {} ID: {}, {} -> {}",
                        entityType, getEntityId(entity), currentImageUrl, objectKey);
            } else {
                log.warn("⚠️ S3 URL 패턴이 아님 - {} ID: {}, URL: {}",
                        entityType, getEntityId(entity), currentImageUrl);
            }
        }

        logMigrationStats(entityType, entities.size(), migratedCount, defaultImageCount, alreadyMigratedCount);
    }

    // 추상 메서드들 - 각 구현체에서 정의
    protected abstract String getEntityType();
    protected abstract List<T> getAllEntities();
    protected abstract String getCurrentImageUrl(T entity);
    protected abstract Object getEntityId(T entity);
    protected abstract void updateImageUrl(T entity, String objectKey);

    // 기본 이미지 관련 메서드들 (필요한 경우만 오버라이드)
    protected boolean hasDefaultImage() {
        return false;
    }

    protected boolean isDefaultImage(String imageUrl) {
        return false;
    }

    protected void updateToDefaultObjectKey(T entity) {
        // 기본 구현은 빈 메서드
    }

    // 유틸리티 메서드들
    protected String extractObjectKeyFromS3Url(String s3Url) {
        if (s3Url == null || s3Url.trim().isEmpty()) {
            return null;
        }

        Matcher matcher = S3_URL_PATTERN.matcher(s3Url.trim());
        if (matcher.matches()) {
            return matcher.group(1);
        }

        return null;
    }

    protected boolean isAlreadyObjectKey(String imageUrl) {
        return imageUrl != null &&
                !imageUrl.trim().isEmpty() &&
                !imageUrl.startsWith("https://") &&
                !imageUrl.startsWith("http://");
    }

    private void logMigrationStats(String entityType, int totalCount, int migratedCount,
                                   int defaultImageCount, int alreadyMigratedCount) {
        log.info("🎉 {} 이미지 URL 마이그레이션 완료", entityType);

        if (defaultImageCount > 0) {
            log.info("📈 마이그레이션 통계 - S3 URL 변경: {}개, 기본 이미지 변경: {}개, 이미 완료: {}개, 전체: {}개",
                    migratedCount, defaultImageCount, alreadyMigratedCount, totalCount);
        } else {
            log.info("📈 마이그레이션 통계 - 변경: {}개, 이미 완료: {}개, 전체: {}개",
                    migratedCount, alreadyMigratedCount, totalCount);
        }
    }
}
