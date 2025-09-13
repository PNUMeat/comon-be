package site.codemonster.comon.global.images.migration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.entity.ArticleImage;
import site.codemonster.comon.domain.article.repository.ArticleImageRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // ArticleImageMigration 다음에 실행
public class ArticleBodyImageMigration implements CommandLineRunner {

    private final ArticleImageRepository articleImageRepository;

    @Value("${cloud.aws.bucket}")
    private String s3Bucket;

    @Value("${cloud.aws.region}")
    private String s3Region;

    @Transactional
    @Override
    public void run(String... args) {
        String entityType = "게시글 본문 이미지";
        log.info("🚀 {} 마이그레이션 시작", entityType);

        List<ArticleImage> articleImages = articleImageRepository.findAll();
        log.info("📊 마이그레이션 대상 {} 개수: {}", entityType, articleImages.size());

        int placeholderReplacedCount = 0;    // 경우 1: ? 치환
        int oldBucketReplacedCount = 0;      // 경우 2: 구버전 버킷 URL 치환
        int alreadyUpdatedCount = 0;         // 경우 3: 이미 업데이트됨
        int orphanedImageCount = 0;          // 경우 4: 사용되지 않는 이미지

        for (ArticleImage articleImage : articleImages) {
            Article article = articleImage.getArticle();
            String originalBody = article.getArticleBody();
            String imageObjectKey = articleImage.getImageUrl(); // 객체 키 (예: article/dee6026b-6fd6-4d1c-bd42-25894b3b846c.png)
            String fullImageUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", s3Bucket, s3Region, imageObjectKey);
            String updatedBody = originalBody;

            if (originalBody.contains("img src=\"?\"") || originalBody.contains("img src=\"\"")) {
                // 경우 1: img src="?" 또는 img src=""인 경우
                updatedBody = originalBody.replaceFirst("img src=\"(\\?|\")\"", "img src=\"" + fullImageUrl + "\"");
                article.updateArticle(article.getArticleTitle(), updatedBody);
                placeholderReplacedCount++;
                log.info("🔄 임시 이미지 태그 치환 완료 - 게시글 ID: {}, 이미지 ID: {}",
                        article.getArticleId(), articleImage.getArticleImageId());

            } else if (originalBody.matches(".*https://[^/]+\\.s3\\.[^/]+\\.amazonaws\\.com/.*") &&
                    !originalBody.contains(s3Bucket)) {
                // 경우 2: 현재 버킷이 아닌 모든 S3 URL (구버킷으로 간주)
                updatedBody = originalBody.replaceAll(
                        "https://[^/]+\\.s3\\.[^/]+\\.amazonaws\\.com/[^\"\\s<>]+",
                        fullImageUrl
                );
                article.updateArticle(article.getArticleTitle(), updatedBody);
                oldBucketReplacedCount++;
                log.info("🔄 구버전 버킷 URL 치환 완료 - 게시글 ID: {}, 이미지 ID: {}",
                        article.getArticleId(), articleImage.getArticleImageId());

            } else if (originalBody.contains(fullImageUrl) || originalBody.contains(imageObjectKey)) {
                // 경우 3: 이미 이미지 URL이 업데이트됨 (전체 URL이나 객체 키 둘 다 체크)
                alreadyUpdatedCount++;
                log.debug("✅ 이미 이미지 URL이 업데이트됨 - 게시글 ID: {}, 이미지 ID: {}",
                        article.getArticleId(), articleImage.getArticleImageId());

            } else {
                // 경우 4: 게시글에서 이미지가 삭제되었지만 articleImage 테이블에는 남아있음
                orphanedImageCount++;
                log.warn("⚠️ 본문에서 이미지를 찾을 수 없음 (고아 이미지) - 게시글 ID: {}, 이미지 ID: {}",
                        article.getArticleId(), articleImage.getArticleImageId());
            }
        }

        logMigrationStats(entityType, articleImages.size(), placeholderReplacedCount,
                oldBucketReplacedCount, alreadyUpdatedCount, orphanedImageCount);
    }

    private void logMigrationStats(String entityType, int totalCount, int placeholderReplacedCount,
                                   int oldBucketReplacedCount, int alreadyUpdatedCount, int orphanedImageCount) {
        log.info("🎉 {} 마이그레이션 완료", entityType);
        log.info("📈 마이그레이션 통계 - 플레이스홀더 치환: {}개, 구버전 URL 치환: {}개, 이미 완료: {}개, 고아 이미지: {}개, 전체: {}개",
                placeholderReplacedCount, oldBucketReplacedCount, alreadyUpdatedCount, orphanedImageCount, totalCount);

        int successCount = placeholderReplacedCount + oldBucketReplacedCount;
        log.info("✨ 총 {}개의 게시글 본문 이미지 치환 완료", successCount);
    }
}
