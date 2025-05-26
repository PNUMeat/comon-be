package site.codemonster.comon.global.images.enums;

public enum ImageCategory {
	ARTICLE("article"), // 게시글 작성에 사용되는 이미지
	PROFILE("profile"), // 프로필 사진
	TEAM("team"), // 팀 사진
	TEAM_RECRUIT("team_recruit"); // 팀 모집 게시글에 사용되는 이미지

	private final String folderName;

	ImageCategory(String folderName) {
		this.folderName = folderName;
	}

	public String getFolderName() {
		return folderName;
	}
}
