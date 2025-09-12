package site.codemonster.comon.global.images.migration;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import site.codemonster.comon.domain.article.entity.ArticleImage;
import site.codemonster.comon.domain.article.repository.ArticleImageRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Order(1)
public class ArticleImageMigration extends BaseImageMigration<ArticleImage> {

    private final ArticleImageRepository articleImageRepository;

    @Override
    protected String getEntityType() {
        return "게시글";
    }

    @Override
    protected List<ArticleImage> getAllEntities() {
        return articleImageRepository.findAll();
    }

    @Override
    protected String getCurrentImageUrl(ArticleImage entity) {
        return entity.getImageUrl();
    }

    @Override
    protected Object getEntityId(ArticleImage entity) {
        return entity.getArticleImageId();
    }

    @Override
    protected void updateImageUrl(ArticleImage entity, String objectKey) {
        entity.updateImageUrl(objectKey);
    }
}
