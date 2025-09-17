package site.codemonster.comon.global.images.enums;

public enum ImageConstant {
    DEFAULT_MEMBER_PROFILE("profile/default-image.png"),
    DEFAULT_TEAM("team/default-image.png");

    private final String objectKey;

    ImageConstant(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getObjectKey() {
        return objectKey;
    }
}
