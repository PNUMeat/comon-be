package site.codemonster.comon.global.util.s3;

import static site.codemonster.comon.global.error.ErrorCode.INVALID_IMAGE_URL_FORMAT;
import site.codemonster.comon.global.error.ComonException;

public final class S3ImageUtil {
    private static String bucketUrl;

    public static void setBucketUrl(String bucket, String region) {
        bucketUrl = String.format("https://%s.s3.%s.amazonaws.com", bucket, region);
    }

    public static String convertObjectKeyToImageUrl(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            return null;
        }
        return bucketUrl + "/" + objectKey;
    }

    public static String convertImageUrlToObjectKey(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        String urlPrefix = bucketUrl + "/";
        if (imageUrl.startsWith(urlPrefix)) {
            return imageUrl.substring(urlPrefix.length());
        } else {
            throw new ComonException(INVALID_IMAGE_URL_FORMAT);
        }
    }

    public static String getBucketUrl() {
        return bucketUrl;
    }

    private S3ImageUtil() {
    }
}
