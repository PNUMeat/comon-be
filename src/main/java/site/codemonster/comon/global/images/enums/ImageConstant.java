package site.codemonster.comon.global.images.enums;

public enum ImageConstant {
    DEFAULT_MEMBER_PROFILE("https://codemonster-s3-bucket-v1.s3.ap-northeast-2.amazonaws.com/profile/default-image.png"),
    DEFAULT_TEAM("https://codemonster-s3-bucket-v1.s3.ap-northeast-2.amazonaws.com/team/default-image.png");

    private final String objectKey;

    ImageConstant(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getObjectKey() {
        return objectKey;
    }
}
