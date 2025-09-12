package site.codemonster.comon.global.images.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import site.codemonster.comon.domain.article.entity.ArticleImage;
import site.codemonster.comon.domain.article.repository.ArticleImageRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleImageUrlMigrationRunner implements CommandLineRunner {

    private final ArticleImageRepository articleImageRepository;

    // S3 URL에서 객체 키를 추출하는 정규식 패턴
    private static final Pattern S3_URL_PATTERN = Pattern.compile(
            "https://[^/]+\\.s3\\.[^/]+\\.amazonaws\\.com/(.+)"
    );

    @Transactional
    @Override
    public void run(String... args) {
        log.info("🚀 게시글 이미지 URL 마이그레이션 시작");

        List<ArticleImage> articleImages = articleImageRepository.findAll();
        log.info("📊 마이그레이션 대상 이미지 개수: {}", articleImages.size());

        int migratedCount = 0;
        int alreadyMigratedCount = 0;

        for (ArticleImage articleImage : articleImages) {
            String currentImageUrl = articleImage.getImageUrl();

            if (isAlreadyObjectKey(currentImageUrl)) {
                alreadyMigratedCount++;
                log.debug("✅ 이미 객체 키 형태 - 이미지 ID: {}, URL: {}",
                        articleImage.getArticleImageId(), currentImageUrl);
                continue;
            }

            String objectKey = extractObjectKeyFromS3Url(currentImageUrl);
            if (objectKey != null) {
                updateImageUrl(articleImage, objectKey);
                migratedCount++;
                log.info("🔄 URL 마이그레이션 완료 - 이미지 ID: {}, {} -> {}",
                        articleImage.getArticleImageId(), currentImageUrl, objectKey);
            } else {
                log.warn("⚠️ S3 URL 패턴이 아님 - 이미지 ID: {}, URL: {}",
                        articleImage.getArticleImageId(), currentImageUrl);
            }
        }

        log.info("🎉 게시글 이미지 URL 마이그레이션 완료");
        log.info("📈 마이그레이션 통계 - 변경: {}개, 이미 완료: {}개, 전체: {}개",
                migratedCount, alreadyMigratedCount, articleImages.size());
    }

    /**
     * S3 URL에서 객체 키를 추출합니다.
     *
     * @param s3Url S3 전체 URL
     * @return 객체 키 (예: "article/dee6026b-6fd6-4d1c-bd42-25894b3b846c.png")
     */
    private String extractObjectKeyFromS3Url(String s3Url) {
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
    private boolean isAlreadyObjectKey(String imageUrl) {
        return imageUrl != null &&
                !imageUrl.trim().isEmpty() &&
                !imageUrl.startsWith("https://") &&
                !imageUrl.startsWith("http://");
    }

    /**
     * ArticleImage의 imageUrl을 업데이트합니다.
     * (실제 업데이트를 위해서는 ArticleImage 엔티티에 setter 또는 update 메서드 필요)
     */
    private void updateImageUrl(ArticleImage articleImage, String objectKey) {
        // ArticleImage 엔티티에 updateImageUrl 메서드를 추가해야 합니다.
        // articleImage.updateImageUrl(objectKey);

        // 또는 리플렉션을 사용한 임시 방법 (권장하지 않음)
        try {
            var field = ArticleImage.class.getDeclaredField("imageUrl");
            field.setAccessible(true);
            field.set(articleImage, objectKey);
        } catch (Exception e) {
            log.error("❌ 이미지 URL 업데이트 실패 - 이미지 ID: {}", articleImage.getArticleImageId(), e);
        }
    }
}
