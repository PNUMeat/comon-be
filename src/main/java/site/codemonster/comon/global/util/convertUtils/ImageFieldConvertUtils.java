package site.codemonster.comon.global.util.convertUtils;

import static site.codemonster.comon.global.error.ErrorCode.INVALID_IMAGE_URL_FORMAT;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.globalConfig.AwsProperties;

@Component
@RequiredArgsConstructor
public class ImageFieldConvertUtils {
    private final AwsProperties awsProperties;

    public String convertObjectKeyToImageUrl(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            return null;
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s", awsProperties.getBucket(), awsProperties.getRegion(), objectKey);
    }

    public String convertImageUrlToObjectKey(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        String urlPrefix = String.format("https://%s.s3.%s.amazonaws.com/", awsProperties.getBucket(), awsProperties.getRegion());
        if (imageUrl.startsWith(urlPrefix)) {
            return imageUrl.substring(urlPrefix.length());
        } else {
            throw new ComonException(INVALID_IMAGE_URL_FORMAT);
        }
    }
}
